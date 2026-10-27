"""Portable updater regressions. All profiles and worlds are synthetic."""
import copy
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import zipfile
from updater import core
from updater.discovery import discover, identify
from updater.selftest import fixture, run, write_fixture
from updater.versions import version_tuple

class UpdaterTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)/'Instances'/'Gregstead ää space'
        self.root.mkdir(parents=True)
        self.base,self.target,self.spec = fixture()
        for name,data in self.base.items():
            core.atomic(self.root/name,data)

    def test_packaged_engine_preserves_world_maps_and_personal_choices(self):
        self.assertTrue(run()['passed'])

    def test_wrong_base_and_missing_unchanged_binary_block_before_changes(self):
        (self.root/'mods/example-1.jar').write_bytes(b'changed')
        before = {p.relative_to(self.root).as_posix():p.read_bytes() for p in self.root.rglob('*') if p.is_file()}
        with self.assertRaisesRegex(core.UpdateError,'modified file'):
            core.apply_update(self.root,self.spec,self.target)
        after = {p.relative_to(self.root).as_posix():p.read_bytes() for p in self.root.rglob('*') if p.is_file()}
        self.assertEqual(before,after)

    def test_shared_conflict_blocks_but_mixed_personal_changes_are_preserved(self):
        p = self.root/'config/gtceu.yaml'
        p.write_bytes(self.base['config/gtceu.yaml'].replace(b'fancy: false',b'fancy: true'))
        plan = core.plan_update(self.root,self.spec,self.target)
        self.assertIn(b'fancy: true',plan['writes']['config/gtceu.yaml'])
        p.write_bytes(p.read_bytes().replace(b'harder: false',b'harder: maybe'))
        with self.assertRaisesRegex(core.UpdateError,'differs from both'):
            core.plan_update(self.root,self.spec,self.target)

    def test_running_game_and_stale_lock_block_update(self):
        with patch('updater.core.running_game',return_value=True):
            with self.assertRaisesRegex(core.UpdateError,'Close Minecraft'):
                core.apply_update(self.root,self.spec,self.target)
        (self.root/core.LOCK).write_text('{}')
        with self.assertRaisesRegex(core.UpdateError,'interrupted update'):
            core.apply_update(self.root,self.spec,self.target)

    def test_failure_after_writing_restores_old_bytes_and_receipt(self):
        core.atomic(self.root/core.RECEIPT,core.encoded({'fingerprint':self.spec['base']['fingerprint'],'version':'1.0.26'}))
        old_receipt = (self.root/core.RECEIPT).read_bytes()
        real = core.atomic
        calls = []
        def fail_one(path,data):
            if Path(path) == self.root/'mods/example-2.jar' and not calls:
                calls.append(path)
                raise OSError('synthetic disk failure')
            return real(path,data)
        with patch('updater.core.atomic',side_effect=fail_one):
            with self.assertRaisesRegex(OSError,'disk failure'):
                core.apply_update(self.root,self.spec,self.target)
        for name,data in self.base.items():
            self.assertEqual((self.root/name).read_bytes(),data)
        self.assertEqual((self.root/core.RECEIPT).read_bytes(),old_receipt)
        self.assertFalse((self.root/'mods/example-2.jar').exists())
        self.assertFalse((self.root/core.LOCK).exists())
        journal = json.loads(next((self.root/'.gregstead-backups').glob('*/journal.json')).read_text())
        self.assertEqual(journal['phase'],'rolled-back')

    def test_explicit_recovery_of_crashed_update(self):
        operation = 'a'*32
        backup = self.root/'.gregstead-backups'/operation
        name = 'mods/example-1.jar'
        core.atomic(backup/'files'/name,self.base[name])
        core.atomic(backup/'journal.json',core.encoded({'phase':'applying','before':{name:{'sha256':core.sha(self.base[name]),'mode':0o644},'mods/example-2.jar':None}}))
        (self.root/name).unlink()
        core.atomic(self.root/'mods/example-2.jar',b'incomplete')
        core.recover(self.root,operation)
        self.assertEqual((self.root/name).read_bytes(),self.base[name])
        self.assertFalse((self.root/'mods/example-2.jar').exists())

    def test_modified_shared_config_cannot_be_hidden_by_matching_receipt(self):
        core.apply_update(self.root,self.spec,self.target)
        p=self.root/'config/gtceu.yaml';p.write_bytes(p.read_bytes().replace(b'harder: true',b'harder: false'))
        report=core.inspect_profile(self.root,self.spec['target'])
        self.assertFalse(report['matches'])
        self.assertIn('config/gtceu.yaml',report['differences'])

    def test_duplicate_case_windows_reserved_names_and_traversal_rejected(self):
        for name in ['../world/level.dat','mods/../world','mods//a.jar','mods/a.jar:stream','mods/CON.jar','config/x.','mods/a\\b.jar']:
            with self.assertRaises(core.UpdateError,msg=name):core.relative(name)
        bundle=Path(self.tmp.name)/'bad.zip'
        with zipfile.ZipFile(bundle,'w') as z:
            z.writestr('payload/mods/A.jar',b'a');z.writestr('payload/mods/a.jar',b'b')
        with self.assertRaisesRegex(core.UpdateError,'duplicate'):
            core.read_bundle(bundle)

    @unittest.skipIf(__import__('os').name=='nt','Creating symlinks may require Windows developer mode')
    def test_managed_symlink_cannot_redirect_writes(self):
        outside=Path(self.tmp.name)/'outside';outside.mkdir()
        (self.root/'config/gtceu.yaml').unlink()
        (self.root/'config/gtceu.yaml').symlink_to(outside/'config.yaml')
        with self.assertRaisesRegex(core.UpdateError,'Symlink'):
            core.plan_update(self.root,self.spec,self.target)
        self.assertFalse((outside/'config.yaml').exists())

    def test_bundle_verifies_payload_and_rejects_extra_members(self):
        path=write_fixture(Path(self.tmp.name)/'good.zip')
        spec,data=core.read_bundle(path)
        self.assertEqual(data,self.target)
        with zipfile.ZipFile(path,'a') as z:z.writestr('AGENTS.md','not distributable')
        with self.assertRaisesRegex(core.UpdateError,'Unexpected'):
            core.read_bundle(path)

    def test_discovery_does_not_choose_unrelated_or_same_named_profile(self):
        unknown=self.root.parent/'Gregstead';unknown.mkdir()
        self.assertIsNone(identify(unknown,self.spec['base']))
        result=discover(self.spec['base'],[self.root.parent])
        self.assertEqual([r['path'] for r in result],[str(self.root)])
        core.atomic(unknown/'minecraftinstance.json',core.encoded({'name':'Renamed by user','installedModpack':{'addonID':1768761}}))
        self.assertEqual(len(discover(self.spec['base'],[self.root.parent])),2)

    def test_wrong_minecraft_or_loader_is_not_patched(self):
        for cf in [{'gameVersion':'26.2'}, {'gameVersion':'26.3','baseModLoader':{'name':'neoforge-26.3.0.100'}}]:
            core.atomic(self.root/'minecraftinstance.json',core.encoded(cf))
            with self.assertRaises(core.UpdateError):core.plan_update(self.root,self.spec,self.target)

    def test_remote_observation_uses_identical_shared_engine(self):
        files={n:core.sha(b) for n,b in self.base.items()}
        observation={'files':files,'contents':{n:b.hex() for n,b in self.base.items() if n.startswith('config/')},
                     'mods':['example-1.jar'],'modHashes':{'example-1.jar':files['mods/example-1.jar']}}
        local=core.plan_update(self.root,self.spec,self.target)
        remote=core.plan_update('/nonexistent/server',self.spec,self.target,observation)
        self.assertEqual(local['writes'],remote['writes'])
        self.assertEqual(local['remove'],remote['remove'])

    def test_unapproved_extra_mods_block_but_pinned_server_extras_are_allowed(self):
        core.atomic(self.root/'mods/extra.jar',b'extra')
        with self.assertRaisesRegex(core.UpdateError,'Additional mod'):
            core.plan_update(self.root,self.spec,self.target)
        spec=copy.deepcopy(self.spec);spec['side']='server'
        spec['base']['files']['server']=spec['base']['files']['client']
        spec['target']['files']['server']=spec['target']['files']['client']
        for key in ('base','target'):spec[key]['fingerprint']=core.fingerprint(spec[key])
        self.assertTrue(core.plan_update(self.root,spec,self.target,allowed_extras={'extra.jar':core.sha(b'extra')})['writes'])
        with self.assertRaises(core.UpdateError):core.plan_update(self.root,spec,self.target,allowed_extras={'extra.jar':'0'*64})

    def test_json_key_migration_keeps_unowned_preferences(self):
        policy={'kind':'mixed','format':'json','ownedKeys':['required']}
        actual=b'{"required":false,"myKey":"custom","extra":42}'
        desired=b'{"required":true,"myKey":"default"}'
        merged=json.loads(core.merged_config(actual,desired,policy))
        self.assertEqual(merged,{'required':True,'myKey':'custom','extra':42})

    def test_gt_format_unquoted_punctuation_is_retained(self):
        policy={'kind':'mixed','format':'gt-sections','personalSections':['client']}
        value=b'recipes:\n  prefix: -\nclient:\n  show: true\n'
        self.assertTrue(core.owned_hash(value,policy))
        self.assertIn(b'prefix: -',core.merged_config(value,value,policy))

    def test_prereleases_sort_numerically_and_below_final(self):
        versions=['1.0.26','1.0.27-beta.1','1.0.27-beta.2','1.0.27-beta.10','1.0.27-rc.1','1.0.27','1.0.28']
        self.assertEqual(sorted(reversed(versions),key=version_tuple),versions)
        for value in ['1.0','1.00.1','1.0.27-beta.01','../../1.0.27']:
            with self.assertRaises(ValueError):version_tuple(value)

if __name__=='__main__':unittest.main()
