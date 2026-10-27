"""Synthetic packaged-engine acceptance. Never touches a real game profile."""
import io
import json
from pathlib import Path
import tempfile
import zipfile
from updater import core


def fixture():
    def jar(version):
        stream = io.BytesIO()
        with zipfile.ZipFile(stream,'w') as z:
            z.writestr('META-INF/neoforge.mods.toml',f'[[mods]]\nmodId="gregstead_fixture"\nversion="{version}"\n')
        return stream.getvalue()
    base = {'mods/example-1.jar':jar('1'), 'config/preferences.json':b'{"key":"default"}\n',
            'config/gtceu.yaml':b'recipes:\n  harder: false\nclient:\n  fancy: false\n'}
    target = {'mods/example-2.jar':jar('2'), 'config/preferences.json':b'{"key":"new-default"}\n',
              'config/gtceu.yaml':b'recipes:\n  harder: true\nclient:\n  fancy: false\n'}
    def lock(version, files):
        rows = {}
        for name,data in files.items():
            policy = ({'kind':'binary'} if name.startswith('mods/') else
                      {'kind':'personal'} if 'preferences' in name else
                      {'kind':'mixed','format':'gt-sections','personalSections':['client']})
            rows[name] = {'sha256':core.sha(data),'bytes':len(data),'policy':policy}
            if policy['kind'] == 'mixed':
                rows[name]['ownedHash'] = core.owned_hash(data,policy)
        result = {'schemaVersion':1,'version':version,'sourceCommit':'a'*40,'minecraft':'26.3','neoforge':'26.3.0.77',
                  'files':{'client':rows,'server':{}},'components':[]}
        result['fingerprint'] = core.fingerprint(result)
        return result
    before,after = lock('1.0.26',base),lock('1.0.27-beta.1',target)
    spec = {'schemaVersion':2,'side':'client','base':before,'target':after,
            'sources':{n:{'member':'payload/'+n} for n in target}}
    return base,target,spec


def write_fixture(path):
    _,target,spec = fixture()
    with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED) as z:
        z.writestr('update-manifest.json',core.encoded(spec))
        for name,data in target.items():
            z.writestr('payload/'+name,data)
    return path


def run(bundle=None):
    if bundle:
        core.read_bundle(bundle)
    base,target,spec = fixture()
    with tempfile.TemporaryDirectory(prefix='gregstead-selftest-') as tmp:
        root = Path(tmp)/'Profiles'/'Gregstead å with spaces'
        root.mkdir(parents=True)
        for name,data in base.items():
            core.atomic(root/name,data)
        core.atomic(root/'config/preferences.json',b'{"key":"my-key"}\n')
        core.atomic(root/'config/gtceu.yaml',base['config/gtceu.yaml'].replace(b'fancy: false',b'fancy: true'))
        core.atomic(root/'saves/NeoGregTest/level.dat',b'protected world')
        core.atomic(root/'xaero/world-map/NeoGregTest/map.bin',b'protected map')
        core.atomic(root/'minecraftinstance.json',core.encoded({'name':'Fixture','gameVersion':'26.3'}))
        receipt = core.apply_update(root,spec,target)
        assert receipt['version'] == '1.0.27-beta.1'
        assert not (root/'mods/example-1.jar').exists()
        assert (root/'mods/example-2.jar').read_bytes() == target['mods/example-2.jar']
        assert (root/'config/preferences.json').read_bytes() == b'{"key":"my-key"}\n'
        assert b'fancy: true' in (root/'config/gtceu.yaml').read_bytes()
        assert b'harder: true' in (root/'config/gtceu.yaml').read_bytes()
        assert (root/'saves/NeoGregTest/level.dat').read_bytes() == b'protected world'
        assert (root/'xaero/world-map/NeoGregTest/map.bin').read_bytes() == b'protected map'
        assert core.inspect_profile(root,spec['target'])['matches']
        return {'passed':True,'worldAndMapsPreserved':True,'personalSettingsPreserved':True,
                'sharedSettingsUpdated':True,'oldJarRemoved':True,'receiptVerified':True}
