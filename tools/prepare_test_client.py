#!/usr/bin/env python3
"""Prepare the sole persistent regression profile, without creating a world."""
import hashlib
import fcntl
import json
from pathlib import Path
import shutil
import sys
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from release import artifact_filename

ROOT=Path(__file__).resolve().parents[1]
PROFILE=ROOT/'.test-client'
WORLD='Gregstead-Regression'

def main():
    PROFILE.mkdir(exist_ok=True)
    session_lock=(PROFILE/'session.lock').open('a+')
    try:fcntl.lockf(session_lock,fcntl.LOCK_EX|fcntl.LOCK_NB)
    except BlockingIOError:raise SystemExit('Close the regression client before preparing its profile')
    saves=PROFILE/'saves';saves.mkdir(exist_ok=True)
    unexpected=[p.name for p in saves.iterdir() if p.is_dir() and p.name!=WORLD]
    if unexpected:raise SystemExit('Unexpected worlds: '+str(unexpected))
    statepath=PROFILE/'test-state.json';state=json.loads(statepath.read_text()) if statepath.exists() else {'world':WORLD,'worldCreated':False}
    if (state.get('worldCreated') or (saves/WORLD).exists()) and not (saves/WORLD/'level.dat').exists():raise SystemExit('Persistent world is missing; refusing to replace it')
    state['worldCreated']=(saves/WORLD/'level.dat').exists()
    lock=json.loads((ROOT/'pack/build-inputs.json').read_text());projects=json.loads((ROOT/'projects.json').read_text())['projects']
    mods=PROFILE/'mods';mods.mkdir(exist_ok=True)
    inputs={}
    for r in lock['artifacts']:
        if r['kind']=='shaderpack':
            src=ROOT/'.build/pack-inputs'/r['filename']
            if hashlib.sha256(src.read_bytes()).hexdigest()!=r['sha256']:raise SystemExit('Changed shader input')
            dest=PROFILE/'shaderpacks'/r['filename'];dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(src,dest)
    for p in (ROOT/'pack/shader-defaults').glob('*'):
        if p.is_file():
            dest=PROFILE/'shaderpacks'/p.name
            if not dest.exists():shutil.copy2(p,dest)
    for r in lock['artifacts']:
        if r['kind']=='mod' and 'client' in r['sides']:
            p=ROOT/'.build/pack-inputs'/r['filename']
            if hashlib.sha256(p.read_bytes()).hexdigest()!=r['sha256']:raise SystemExit('Changed input '+str(p))
            inputs[p.name]=p
    for c,p in projects.items():
        if p['kind']=='mod' and p['side'] in ['both','client'] and c!='gregtech':
            jar=ROOT/'dist'/c/artifact_filename(c);inputs[jar.name]=jar
    old=state.get('managedMods',{})
    for name in old.keys()-inputs.keys():(mods/name).unlink(missing_ok=True)
    unknown={p.name for p in mods.glob('*.jar')}-set(inputs)
    if unknown:raise SystemExit('Unmanaged test mods: '+str(unknown))
    for name,p in inputs.items():shutil.copy2(p,mods/name)
    for p in (ROOT/'pack/client-defaults').rglob('*'):
        if p.is_file():
            target=PROFILE/p.relative_to(ROOT/'pack/client-defaults')
            if not target.exists():target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,target)
    runtime=json.loads((ROOT/'pack/client-runtime.json').read_text())
    for row in runtime['libraries']:
        dest=PROFILE/row['relativePath'];dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT/'.build/pack-inputs'/row['filename'],dest)
    state['candidateSha256']=hashlib.sha256((ROOT/'dist/gregtech'/artifact_filename('gregtech')).read_bytes()).hexdigest()
    state.update(baseline='1.0.27',candidate=artifact_filename('gregtech'),automaticBackups=False,managedMods={n:hashlib.sha256(p.read_bytes()).hexdigest() for n,p in inputs.items()})
    statepath.write_text(json.dumps(state,indent=2)+'\n')
    init=ROOT/'.build/regression-client.init.gradle'
    init.write_text('''gradle.beforeProject { p ->
  if (p.path == ':') p.plugins.withId('net.neoforged.moddev') {
    p.tasks.matching { it.name == 'downloadAssets' }.configureEach { javaExecutable = System.getenv('JAVA_HOME') + '/bin/java' }
    def probe = p.sourceSets.create('regressionProbe')
    probe.java.srcDir(p.file('../../../tools/regression_probe/java'))
    probe.resources.srcDir(p.file('../../../tools/regression_probe/resources'))
    probe.compileClasspath += p.sourceSets.main.compileClasspath + p.sourceSets.main.output
    probe.runtimeClasspath += probe.compileClasspath
    p.neoForge.mods.create('gregstead_regression_probe') { sourceSet(probe) }
    p.neoForge.runs.create('regressionClient') {
        client()
        gameDirectory = p.file('''+json.dumps(str(PROFILE))+''')
        programArguments.addAll('--username', 'GregsteadTest')
    }
  }
}
''')
    print('Profile prepared:',PROFILE,'world created:',state['worldCreated'])

if __name__=='__main__':main()
