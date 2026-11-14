#!/usr/bin/env python3
"""Build client and server ZIPs from versioned inputs; never install or publish."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import tomllib
import urllib.request
import zipfile
from release import artifact_filename

ROOT = Path(__file__).resolve().parent
CACHE = ROOT / '.build/pack-inputs'


def digest(data):
    return hashlib.sha256(data).hexdigest()


def artifact(row, online=False, artifact_dir=None):
    path = CACHE / row['filename']
    if not path.exists():
        local = artifact_dir / row['filename'] if artifact_dir else None
        if local and local.is_file():
            data = local.read_bytes()
        elif online:
            print('Downloading ' + row['filename'], flush=True)
            request = urllib.request.Request(row['url'], headers={'User-Agent': 'Gregstead-build/1'})
            with urllib.request.urlopen(request, timeout=120) as response:
                data = response.read()
        else:
            raise ValueError('Missing ' + row['filename'] + '; run bootstrap --download-dependencies')
        if digest(data) != row['sha256']:
            raise ValueError('Input checksum differs: ' + row['filename'])
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    data = path.read_bytes()
    if digest(data) != row['sha256']:
        raise ValueError('Cached input checksum differs: ' + row['filename'])
    return data


def tree(folder):
    base = ROOT / 'pack' / folder
    return {p.relative_to(base).as_posix(): p.read_bytes() for p in base.rglob('*') if p.is_file()}


def write_zip(path, payload):
    temporary = path.with_suffix('.zip.tmp')
    try:
        with zipfile.ZipFile(temporary, 'w', zipfile.ZIP_DEFLATED) as archive:
            for name, data in sorted(payload.items()):
                info = zipfile.ZipInfo(name, (2026, 1, 1, 0, 0, 0))
                info.compress_type = zipfile.ZIP_DEFLATED
                info.external_attr = 0o100644 << 16
                archive.writestr(info, data)
        with zipfile.ZipFile(temporary) as archive:
            if archive.testzip() is not None:
                raise ValueError('Archive integrity check failed')
        if path.exists() and path.read_bytes() != temporary.read_bytes():
            raise ValueError('Output already exists with different content: ' + str(path))
        temporary.replace(path)
        path.with_suffix('.zip.sha256').write_text(digest(path.read_bytes()) + '  ' + path.name + '\n')
        print(path.relative_to(ROOT))
    finally:
        temporary.unlink(missing_ok=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['bootstrap', 'build'])
    parser.add_argument('--download-dependencies', action='store_true')
    parser.add_argument('--artifact-dir', type=Path)
    parser.add_argument('--curseforge', action='store_true',
                        help='Require approved component files and use manifest references')
    parser.add_argument('--version', help='Explicit local pack version, e.g. 1.0.26-rebuilt.1')
    parser.add_argument('--github', action='store_true', help='Build GitHub client and server distributions')
    args = parser.parse_args()
    if args.github and (args.curseforge or args.action != 'build'):
        parser.error('--github requires build and excludes --curseforge')
    if args.action == 'bootstrap' and not args.download_dependencies and not args.artifact_dir:
        parser.error('Use --download-dependencies or --artifact-dir for bootstrap')
    if args.action == 'build' and (args.download_dependencies or args.artifact_dir):
        parser.error('Build is offline; use bootstrap to prepare inputs')
    if args.action == 'build' and not re.fullmatch(r'[0-9]+\.[0-9]+\.[0-9]+(?:-[A-Za-z0-9.-]+)?', args.version or ''):
        parser.error('Build requires an explicit --version')
    lock = json.loads((ROOT / 'pack/build-inputs.json').read_text())
    references = {}
    if args.curseforge:
        if args.action != 'build':
            parser.error('--curseforge applies only to build')
        references = json.loads((ROOT / 'pack/component-files.json').read_text())
        projects = json.loads((ROOT / 'projects.json').read_text())['projects']
        for component, project in projects.items():
            if project['kind'] != 'mod':
                continue
            ref = references.get(component, {})
            if ref.get('lastObservedStatus') != 'Approved' or ref.get('version') != project['version']:
                parser.error('CurseForge approval missing for current component: ' + component)
        for row in lock['artifacts']:
            if 'client' in row['sides'] and not row.get('fileID'):
                parser.error('CurseForge file reference missing: ' + row['filename'])
    runtime = json.loads((ROOT / 'pack/client-runtime.json').read_text())
    # The runtime metadata uses downloadUrl; the other pinned inputs use url.
    libraries = [{**r, 'url': r['downloadUrl']} for r in runtime['libraries']]
    inputs = lock['artifacts'] + libraries
    if args.action == 'bootstrap':
        for row in inputs:
            artifact(row, args.download_dependencies, args.artifact_dir)
        return

    own_mods = {}
    client, server = tree('client-defaults'), tree('server-defaults')
    client.update({'shaderpacks/' + n: d for n, d in tree('shader-defaults').items()})
    files, inventory = [], []
    for row in lock['artifacts']:
        data = artifact(row)
        folder = 'shaderpacks/' if row['kind'] == 'shaderpack' else 'mods/'
        if 'server' in row['sides']:
            server[folder + row['filename']] = data
        if 'client' in row['sides']:
            if row.get('fileID'):
                files.append({'projectID': row['projectID'], 'fileID': row['fileID'], 'required': True})
            else:
                client[folder + row['filename']] = data
        inventory.append({k: row[k] for k in ['filename', 'sha256', 'sides']})
    projects = json.loads((ROOT / 'projects.json').read_text())['projects']
    for component, project in projects.items():
        if project['kind'] != 'mod':
            continue
        jars = [ROOT / 'dist' / component / artifact_filename(component)]
        jars = [p for p in jars if p.is_file()]
        if len(jars) != 1:
            raise ValueError('Build exactly one JAR first: ' + component)
        jar = jars[0]
        data = jar.read_bytes()
        checksum = jar.with_suffix('.jar.sha256').read_text().split()[0]
        if digest(data) != checksum:
            raise ValueError('Built JAR checksum differs: ' + component)
        if args.curseforge:
            ref = references[component]
            if ref['filename'] != jar.name or ref['sha256'] != checksum:
                raise ValueError('CurseForge artifact differs from local build: ' + component)
        with zipfile.ZipFile(jar) as archive:
            metadata = tomllib.loads(archive.read('META-INF/neoforge.mods.toml').decode())
        if metadata['mods'][0]['version'] != project['version']:
            raise ValueError('Built JAR version differs: ' + component)
        sides = ['client', 'server'] if project['side'] == 'both' else [project['side']]
        if 'client' in sides:
            own_mods['mods/' + jar.name] = data
            if args.curseforge:
                files.append({'projectID': ref['projectID'], 'fileID': ref['fileID'], 'required': True})
            else:
                client['mods/' + jar.name] = data
        if 'server' in sides:
            server['mods/' + jar.name] = data
        inventory.append({'filename': jar.name, 'sha256': digest(data), 'sides': sides})
    for row in libraries:
        client[row['relativePath']] = artifact(row)
        inventory.append({'filename': row['filename'], 'sha256': row['sha256'], 'sides': ['client']})
    license_data = (ROOT / runtime['licenseFile']).read_bytes()
    if digest(license_data) != runtime['licenseSha256']:
        raise ValueError('ASM license checksum differs')
    client['Gregstead_Info/ASM_LICENSE.txt'] = license_data
    server['eula.txt'] = b'eula=false\n'
    server['INSTALL.txt'] = ('Install JDK 25 and the official NeoForge ' + lock['neoforge'] +
                             ' server installer here. Review the Minecraft EULA before accepting it.\n').encode()
    report = json.dumps({'version': args.version, 'artifacts': inventory}, indent=2).encode() + b'\n'
    client['Gregstead_Info/build-manifest.json'] = report
    server['Gregstead_Info/build-manifest.json'] = report
    manifest = {'minecraft': {'version': lock['minecraft'], 'modLoaders': [
        {'id': 'neoforge-' + lock['neoforge'], 'primary': True}]},
        'manifestType': 'minecraftModpack', 'manifestVersion': 1, 'name': 'Gregstead',
        'version': args.version, 'author': 'Gregstead', 'files': files, 'overrides': 'overrides'}
    payload = {'overrides/' + name: data for name, data in client.items()}
    payload['manifest.json'] = json.dumps(manifest, indent=2).encode() + b'\n'
    if args.github:
        output = ROOT / 'dist/github' / args.version
        output.mkdir(parents=True, exist_ok=True)
        notices = (ROOT / 'THIRD_PARTY_NOTICES.md').read_bytes()
        source = ('Matching source: https://github.com/mmyllynen/gregstead/tree/v' + args.version + '\nBuild instructions: BUILDING.md in that source tree.\n').encode()
        payload['overrides/Gregstead_Info/THIRD_PARTY_NOTICES.md'] = notices
        payload['overrides/Gregstead_Info/SOURCE.txt'] = source
        instructions = ('Close Minecraft. This archive updates ONLY Gregstead-owned client mods in an existing Gregstead installation.\nRemove chunkloaders-*.jar (Chunk Loaders) from previous packs; use OPAC forceload instead. Keep all other third-party mods and all configuration files. Do not replace the entire mods directory.\nRemove previous JARs for the following components before copying mods/ into your instance:\nGregTech (gtceu-* or gregstead-VERSION-*), EMI (emi-native-* or emi-VERSION-*),\nAdditional Placements (additionalplacements-* or additional-placements-*),\nCalm, Altar (altarofabundance-*), UI Fixes, EMI Defaults, AE2 Fixes, Toolbelt Slot, Sleepwear (sleepwear-*).\nFor the latter components remove their previous gregstead-COMPONENT-* JARs.\nDo not remove unrelated mods. Never load two versions of the same mod.\nNo graphics settings, bookmarks, keybindings, worlds, shaders or runtime files are included or replaced.\nRequires the existing Gregstead third-party mod set, Minecraft 26.3, NeoForge 26.3.0.77 and Java 25.\nFor a fresh installation import the full client ZIP into a NEW profile.\n').encode()
        own_mods['INSTALL.txt'] = instructions
        own_mods['THIRD_PARTY_NOTICES.md'] = notices
        own_mods['SOURCE.txt'] = source
        for name, data in tree('..' + '/licenses').items():
            own_mods['licenses/' + name] = data
            payload['overrides/Gregstead_Info/licenses/' + name] = data
        own_mods['SHA256SUMS'] = ''.join(digest(d) + '  ' + n + '\n' for n,d in sorted(own_mods.items()) if n.startswith('mods/')).encode()
        write_zip(output / ('gregstead-client-' + args.version + '-mc26.3.zip'), payload)
        write_zip(output / ('gregstead-client-mods-only-' + args.version + '-mc26.3.zip'), own_mods)
        write_zip(output / ('gregstead-server-' + args.version + '-mc26.3.zip'), server)
        return
    output = ROOT / ('dist/curseforge' if args.curseforge else 'dist/pack') / args.version
    output.mkdir(parents=True, exist_ok=True)
    write_zip(output / ('gregstead-client-' + args.version + '-mc' + lock['minecraft'] + '.zip'), payload)
    write_zip(output / ('gregstead-server-' + args.version + '-mc' + lock['minecraft'] + '.zip'), server)


if __name__ == '__main__':
    main()
