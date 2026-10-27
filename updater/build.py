"""Build a native, fixed-version installer; no upload or installation side effects."""
import argparse
import importlib.metadata
from pathlib import Path
import shutil
import subprocess
import sys
import tarfile
import tempfile
from updater import core, SOURCE_FILES


def collect_licenses(output):
    output.mkdir()
    shutil.copy2(Path(__file__).parent/'LICENSE',output/'Gregstead-MIT.txt')
    for package in ('pyinstaller','psutil','PyYAML'):
        dist=importlib.metadata.distribution(package)
        found=False
        for item in dist.files or []:
            path=Path(str(item))
            if path.name.lower().startswith(('license','copying')) and '..' not in path.parts:
                source=Path(dist.locate_file(item))
                if source.is_file():
                    shutil.copy2(source,output/(package+'-'+path.name+'.txt'));found=True
        if not found:
            raise RuntimeError('Missing license text for '+package)
    candidates = {
        'Python':[Path(__import__('sysconfig').get_paths()['stdlib'])/'LICENSE.txt',Path(sys.base_prefix)/'LICENSE.txt',*Path('/usr/share/doc').glob('python3*/copyright')],
        'Tcl':[Path(sys.base_prefix)/'tcl/tcl8.6/license.terms',Path('/usr/share/doc/tcl8.6/copyright')],
        'Tk':[Path(sys.base_prefix)/'tcl/tk8.6/license.terms',Path('/usr/share/doc/tk8.6/copyright')]
    }
    for package,paths in candidates.items():
        source=next((p for p in paths if p.is_file()),None)
        if source is None and sys.platform=='win32' and package in ('Tcl','Tk'):
            import tkinter
            patchlevel=str(tkinter.Tcl().call('info','patchlevel'))
            provenance=__import__('json').loads((Path(__file__).parent/'licenses/provenance.json').read_text())[package]
            source=Path(__file__).parent/'licenses'/(package+'-'+patchlevel+'.txt')
            if patchlevel!=provenance['version'] or not source.is_file() or core.file_sha(source)!=provenance['sha256']:
                raise RuntimeError('Missing reviewed Windows runtime notice for '+package+' '+patchlevel)
        if source is None:
            raise RuntimeError('Missing bundled runtime notice for '+package)
        shutil.copy2(source,output/(package+'-NOTICE.txt'))


def main():
    p=argparse.ArgumentParser()
    p.add_argument('--bundle',type=Path,required=True)
    p.add_argument('--output',type=Path,required=True)
    a=p.parse_args()
    spec,_=core.read_bundle(a.bundle)
    if spec['side']!='client':
        p.error('Use the workspace installer for servers')
    a.output.mkdir(parents=True,exist_ok=True)
    name='Gregstead-Update-'+spec['base']['version']+'-to-'+spec['target']['version']
    name+='-windows-x64' if sys.platform=='win32' else '-linux-x64'
    executable=a.output/(name+('.exe' if sys.platform=='win32' else ''))
    if executable.exists():
        p.error('Installer artifact already exists; do not replace a published version')
    source_hashes={n:core.file_sha(Path(__file__).parent/n) for n in SOURCE_FILES}
    with tempfile.TemporaryDirectory(prefix='gregstead-build-') as tmp:
        tmp=Path(tmp)
        launch=tmp/'launch.py'
        launch.write_text('from updater.gui import main\nraise SystemExit(main())\n')
        collect_licenses(tmp/'licenses')
        shutil.copy2(a.bundle,tmp/'update.zip')
        command=[sys.executable,'-m','PyInstaller','--noconfirm','--clean','--onefile','--name',name,
                 '--paths',str(Path(__file__).resolve().parent.parent),
                 '--distpath',str(a.output.resolve()),'--workpath',str(tmp/'work'),'--specpath',str(tmp),
                 '--add-data',str(tmp/'update.zip')+':.','--add-data',str(tmp/'licenses')+':licenses',
                 '--hidden-import','tkinter','--hidden-import','psutil','--hidden-import','yaml']
        if sys.platform=='win32':command.append('--windowed')
        subprocess.run([*command,str(launch)],check=True)
    report=a.output/(name+'.selftest.json')
    with tempfile.TemporaryDirectory(prefix='gregstead-packaged-test-') as isolated:
        environment={k:v for k,v in __import__('os').environ.items() if k!='PYTHONPATH'}
        subprocess.run([str(executable.resolve()),'--self-test','--report',str(report.resolve())],check=True,cwd=isolated,env=environment)
    if not __import__('json').loads(report.read_text()).get('passed'):
        raise RuntimeError('Packaged installer self-test failed')
    files={n:core.file_sha(Path(__file__).parent/n) for n in SOURCE_FILES}
    if files != source_hashes:
        raise RuntimeError('Updater source changed during build; rebuild in a new output directory')
    receipt={'schemaVersion':1,'filename':executable.name,'sha256':core.file_sha(executable),
             'updateSha256':core.file_sha(a.bundle),'fromVersion':spec['base']['version'],
             'version':spec['target']['version'],'fingerprint':spec['target']['fingerprint'],
             'platform':'windows-x64' if sys.platform=='win32' else 'linux-x64','sourceHashes':files,
             'testOnly':bool(spec.get('testOnly')),
             'packagedSelfTestPassed':True,'guiSmokePassed':False}
    if sys.platform!='win32':
        archive=a.output/(name+'.tar.gz')
        with tarfile.open(archive,'w:gz') as tar:
            tar.add(executable,arcname=executable.name)
            tar.add(Path(__file__).parent/'README.md',arcname='README.md')
        receipt['archive']={'filename':archive.name,'sha256':core.file_sha(archive)}
    core.atomic(a.output/(name+'.build.json'),core.encoded(receipt))
    print(executable)


if __name__=='__main__':main()
