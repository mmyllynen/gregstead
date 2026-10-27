#!/usr/bin/env python3
"""Run an artifact-level Calm AI comparison in a new loopback-only disposable server."""
from pathlib import Path
import argparse,hashlib,json,os,shutil,subprocess,time,zipfile
ROOT=Path(__file__).resolve().parents[3]

def main():
    p=argparse.ArgumentParser();p.add_argument('--jar',type=Path,required=True);p.add_argument('--mode',choices=['baseline','fixed'],required=True);p.add_argument('--fixture',type=Path,required=True);p.add_argument('--runtime',type=Path,default=ROOT/'.local/calm-ai-regression/runtime/libraries');p.add_argument('--scenarios');p.add_argument('--observe-only',action='store_true');a=p.parse_args()
    # The runner deliberately has no remote-host, remote-command or multiplayer-join option.
    jar=a.jar.resolve();fixture=a.fixture.resolve();assert jar.is_file() and fixture.is_file()
    work=ROOT/'.local/calm-ai-regression'/f'{a.mode}-{time.time_ns()}';server=work/'server';mods=server/'mods';mods.mkdir(parents=True)
    (server/'libraries').symlink_to(a.runtime.resolve(),target_is_directory=True)
    shutil.copy2(jar,mods/jar.name);curios=ROOT/'mods/calm/libs/curios-neoforge-17.0.0-beta.3+26.3.jar';shutil.copy2(curios,mods/curios.name)
    cp=os.pathsep.join(str(x) for x in [ROOT/'mods/calm/build/moddev/artifacts/minecraft-patched-26.3.0.77-merged.jar',jar,curios,*sorted(a.runtime.resolve().rglob('*.jar'))])
    classes=work/'classes';classes.mkdir()
    compile_result=subprocess.run([str(ROOT/'core/toolchains/jdk-25/bin/javac'),'-proc:none','-cp',cp,'-d',str(classes),str(Path(__file__).with_name('CalmCombatRegression.java'))])
    if compile_result.returncode:raise SystemExit(compile_result.returncode)
    with zipfile.ZipFile(mods/'calm-regression-test-only.jar','w') as z:
        z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[12,)"\nlicense="MIT"\n[[mods]]\nmodId="neogreg_calm_regression"\nversion="1"\n')
        for f in classes.rglob('*.class'):z.write(f,f.relative_to(classes))
    (server/'eula.txt').write_text('eula=true\n')
    (server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=CalmCombatRegression\nlevel-type=minecraft:flat\ngenerate-structures=false\nview-distance=3\nsimulation-distance=3\nenable-query=false\nenable-rcon=false\nmax-tick-time=-1\npause-when-empty-seconds=-1\n')
    command=[str(ROOT/'core/toolchains/jdk-25/bin/java'),'-Xmx3G','-Xms512M','-XX:ActiveProcessorCount=4','-Dcalm.regression.work='+str(work),'-Dcalm.regression.mode='+a.mode,'-Dcalm.regression.fixture='+str(fixture),'@libraries/net/neoforged/neoforge/26.3.0.77/unix_args.txt','nogui']
    if a.scenarios:command.insert(1,'-Dcalm.regression.scenarios='+a.scenarios)
    if a.observe_only:command.insert(1,'-Dcalm.regression.observeOnly=true')
    print('WORKDIR',work,flush=True)
    with (work/'server.log').open('wb') as log:
        proc=subprocess.Popen(command,cwd=server,stdout=log,stderr=subprocess.STDOUT)
        (work/'pid').write_text(str(proc.pid))
        try:code=proc.wait(timeout=300)
        except subprocess.TimeoutExpired:
            proc.terminate();proc.wait(timeout=20);raise RuntimeError('Regression timed out; see '+str(work/'server.log'))
    report=work/'report.json'
    if not report.exists():raise RuntimeError(f'No test report (exit {code}); see {work}/server.log')
    result=json.loads(report.read_text());result.update(jarSha256=hashlib.sha256((mods/jar.name).read_bytes()).hexdigest(),fixtureSha256=hashlib.sha256(fixture.read_bytes()).hexdigest(),exitCode=code,probeSha256=hashlib.sha256((mods/'calm-regression-test-only.jar').read_bytes()).hexdigest())
    report.write_text(json.dumps(result,indent=2)+'\n');(ROOT/'.local/calm-ai-regression'/f'latest-{a.mode}.json').write_text(json.dumps({'report':str(report)},indent=2)+'\n')
    print(json.dumps(result,indent=2),flush=True)
    if not result['passed'] or code:raise SystemExit(1)
if __name__=='__main__':main()
