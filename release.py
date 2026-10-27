#!/usr/bin/env python3
"""MIT licensed public-source builder. No install, upload, or game launch."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parent
MINECRAFT_JSON_SHA1 = '61fff1316bd50312624a858bbb5eb18fa45208ed'


def prepare_minecraft(online):
    """Pin Mojang inputs and route NeoForm's launcher requests to verified local files."""
    cache = ROOT / '.build/minecraft'
    original = cache / '26.3-official.json'
    if not original.exists() and online:
        url = 'https://piston-meta.mojang.com/v1/packages/' + MINECRAFT_JSON_SHA1 + '/26.3.json'
        with urllib.request.urlopen(url, timeout=60) as response:
            data = response.read()
        if hashlib.sha1(data).hexdigest() != MINECRAFT_JSON_SHA1:
            raise RuntimeError('Mojang version metadata differs from the reviewed input')
        cache.mkdir(parents=True, exist_ok=True)
        original.write_bytes(data)
    if not original.exists() or hashlib.sha1(original.read_bytes()).hexdigest() != MINECRAFT_JSON_SHA1:
        raise RuntimeError('Run bootstrap --download-dependencies to prepare pinned Mojang metadata')
    version = json.loads(original.read_text())
    for kind in ['client', 'server']:
        row = version['downloads'][kind]
        target = cache / (kind + '.jar')
        if not target.exists() and online:
            with urllib.request.urlopen(row['url'], timeout=120) as response:
                data = response.read()
            if hashlib.sha1(data).hexdigest() != row['sha1']:
                raise RuntimeError('Mojang artifact checksum mismatch: ' + kind)
            target.write_bytes(data)
        if not target.exists() or hashlib.sha1(target.read_bytes()).hexdigest() != row['sha1']:
            raise RuntimeError('Missing verified Mojang ' + kind + '; run bootstrap')
        row['url'] = target.as_uri()
    local_version = cache / '26.3.json'
    local_version.write_text(json.dumps(version))
    entry = {'id': '26.3', 'type': 'release', 'url': local_version.as_uri(),
             'time': version['time'], 'releaseTime': version['releaseTime'],
             'sha1': hashlib.sha1(local_version.read_bytes()).hexdigest(), 'complianceLevel': 1}
    manifest = cache / 'version_manifest_v2.json'
    manifest.write_text(json.dumps({'latest': {'release': '26.3', 'snapshot': '26.3'}, 'versions': [entry]}))
    init = ROOT / '.build/gradle-home/init.d/gregstead-public.gradle'
    init.parent.mkdir(parents=True, exist_ok=True)
    (init.parent / 'neogreg-public.gradle').unlink(missing_ok=True)
    init.write_text("allprojects { pluginManager.withPlugin('net.neoforged.moddev') { neoFormRuntime.launcherManifestUrl = " +
                    json.dumps(manifest.as_uri()) + " } }\n")


def sha(path):
    return hashlib.file_digest(path.open('rb'), 'sha256').hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['build', 'bootstrap'])
    parser.add_argument('component')
    parser.add_argument('--download-dependencies', action='store_true')
    parser.add_argument('--artifact-dir', type=Path,
                        help='Official author-downloaded compile JARs, checked against dependencies.json')
    args = parser.parse_args()
    projects = json.loads((ROOT / 'projects.json').read_text())['projects']
    if args.component not in projects or projects[args.component].get('kind') not in ['mod', 'pack-addon']:
        parser.error('Choose a listed mod component')
    online = args.action == 'bootstrap' and args.download_dependencies
    if args.action == 'bootstrap' and not online:
        parser.error('Online preparation requires --download-dependencies')
    if args.action == 'build' and args.download_dependencies:
        parser.error('Use bootstrap for network preparation')
    prepare_minecraft(online)
    for row in sorted(json.loads((ROOT / 'dependencies.json').read_text()), key=lambda r: bool(r.get('extractFrom'))):
        if args.component not in row['components']:
            continue
        dest = ROOT / row['path']
        if row.get('builtComponent'):
            dependency = row['builtComponent']
            command = [sys.executable, str(ROOT / 'release.py'), args.action, dependency]
            if online:
                command.append('--download-dependencies')
            if args.artifact_dir:
                command += ['--artifact-dir', str(args.artifact_dir)]
            subprocess.run(command, cwd=ROOT, check=True)
            jars = list((ROOT / 'dist' / dependency).glob('*.jar'))
            if len(jars) != 1:
                parser.error('Expected one source-built compile dependency: ' + dependency)
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(jars[0], dest)
            continue
        if not dest.exists():
            source = args.artifact_dir / dest.name if args.artifact_dir else None
            if row.get('extractFrom'):
                with zipfile.ZipFile(ROOT / row['extractFrom']) as archive:
                    data = archive.read(row['entry'])
                if hashlib.sha256(data).hexdigest() != row['sha256']:
                    parser.error('Embedded compile dependency checksum mismatch: ' + dest.name)
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(data)
            elif source and source.is_file():
                if sha(source) != row['sha256']:
                    parser.error('Wrong official artifact: ' + dest.name)
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(source, dest)
            elif online and row.get('url'):
                req = urllib.request.Request(row['url'], headers={'User-Agent': 'Gregstead-public-build/1'})
                with urllib.request.urlopen(req, timeout=60) as response:
                    data = response.read()
                if hashlib.sha256(data).hexdigest() != row['sha256']:
                    parser.error('Downloaded checksum mismatch: ' + dest.name)
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(data)
            else:
                parser.error('Obtain official author artifact ' + dest.name +
                             ' and pass --artifact-dir. Source: ' + str(row.get('source')))
        if sha(dest) != row['sha256']:
            parser.error('Compile dependency changed: ' + dest.name)
    env = os.environ.copy()
    for key in ['GH_TOKEN', 'GITHUB_TOKEN', 'JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', 'GRADLE_OPTS']:
        env.pop(key, None)
    env['GRADLE_USER_HOME'] = str(ROOT / '.build/gradle-home')
    env['JAVA_TOOL_OPTIONS'] = '-XX:ActiveProcessorCount=4'
    project = ROOT / ('core/sources/GregTech-Modern' if args.component == 'gregtech' else
                      'core/sources/EMI-native' if args.component == 'emi' else 'mods/' + args.component)
    wrapper = ROOT / 'core/sources/GregTech-Modern/gradlew' if args.component in ['gregtech', 'emi'] else project / 'gradlew'
    command = ['bash', str(wrapper), '-p', str(project), 'jar',
               '--no-daemon', '--no-build-cache', '--console=plain']
    if not online:
        command.append('--offline')
    subprocess.run(command, cwd=ROOT, env=env, check=True)
    jars = [p for p in (project / 'build/libs').glob('*.jar')
            if not p.name.endswith(('-sources.jar', '-javadoc.jar'))]
    if len(jars) != 1:
        parser.error('Expected exactly one release JAR; use a clean checkout')
    output = ROOT / 'dist' / args.component
    output.mkdir(parents=True, exist_ok=True)
    dest = output / jars[0].name
    if dest.exists() and sha(dest) != sha(jars[0]):
        parser.error('Immutable artifact already exists with different content: ' + str(dest))
    shutil.copy2(jars[0], dest)
    dest.with_suffix(dest.suffix + '.sha256').write_text(sha(dest) + '  ' + dest.name + '\n')
    print(dest.relative_to(ROOT))


if __name__ == '__main__':
    main()
