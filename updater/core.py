"""Portable update engine. No workspace, launcher credentials, or server lifecycle code."""
from __future__ import annotations
import copy
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import socket
import tempfile
import time
import tomllib
import urllib.parse
import urllib.request
import uuid
import zipfile

RECEIPT = '.gregstead-release.json'
LOCK = '.gregstead-update.lock'
PROJECT_ID = 1768761
MAX_PAYLOAD = 1024 * 1024 * 1024

class UpdateError(RuntimeError):
    pass


def encoded(value):
    return (json.dumps(value, sort_keys=True, separators=(',', ':'), ensure_ascii=False, default=str) + '\n').encode()


def sha(data):
    return hashlib.sha256(data).hexdigest()


def file_sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1048576), b''):
            h.update(block)
    return h.hexdigest()


def atomic(path, data):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, tmp = tempfile.mkstemp(prefix='.' + path.name, dir=path.parent)
    try:
        with os.fdopen(fd, 'wb') as stream:
            stream.write(data)
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(tmp, path)
    finally:
        Path(tmp).unlink(missing_ok=True)


def relative(name):
    p = PurePosixPath(name)
    if (not name or p.as_posix() != name or p.is_absolute() or '..' in p.parts
            or any(c in name for c in '\\:\x00') or any(ord(c) < 32 for c in name)):
        raise UpdateError('Unsafe path: ' + repr(name))
    for part in p.parts:
        if part.endswith(('.', ' ')) or re.search(r'[<>"|?*]', part):
            raise UpdateError('Nonportable path: ' + name)
        if part.split('.')[0].upper() in {'CON','PRN','AUX','NUL', *(f'COM{i}' for i in range(1,10)), *(f'LPT{i}' for i in range(1,10))}:
            raise UpdateError('Reserved Windows path: ' + name)
    return name


def target_path(root, name):
    relative(name)
    if not name.startswith(('mods/', 'config/', 'shaderpacks/', 'Gregstead_Info/', 'NeoGreg_Info/')):
        raise UpdateError('Outside pack-owned directories: ' + name)
    if name.startswith(('config/neogreg-discord', 'config/operator-private/')):
        raise UpdateError('Operator-owned configuration: ' + name)
    root = Path(root).resolve()
    path = root / name
    for parent in [path, *path.parents]:
        if parent == root:
            break
        if parent.is_symlink():
            raise UpdateError('Symlink in managed path: ' + name)
    if not path.resolve().is_relative_to(root):
        raise UpdateError('Path escapes profile: ' + name)
    return path


def fingerprint(lock):
    return sha(encoded({k:v for k,v in lock.items() if k != 'fingerprint'}))


def validate_lock(lock):
    if lock.get('schemaVersion') != 1 or not re.fullmatch(r'\d+\.\d+\.\d+(?:-[0-9A-Za-z.]+)?', lock.get('version', '')):
        raise UpdateError('Unsupported release lock')
    if fingerprint(lock) != lock.get('fingerprint'):
        raise UpdateError('Release lock fingerprint differs')
    from updater.versions import version_tuple
    version_tuple(lock['version'])
    if set(lock.get('files',{})) != {'client','server'} or not re.fullmatch('[a-f0-9]{40}',lock.get('sourceCommit','')):
        raise UpdateError('Invalid release identity')
    for side in ('client', 'server'):
        seen = set()
        for name, row in lock['files'][side].items():
            target_path(Path(tempfile.gettempdir()) / 'gregstead-validation-root', name)
            if name.casefold() in seen:
                raise UpdateError('Case-insensitive path collision: ' + name)
            seen.add(name.casefold())
            if not re.fullmatch('[a-f0-9]{64}', row.get('sha256','')):
                raise UpdateError('Missing file hash: ' + name)
            if row['policy']['kind'] not in {'binary','managed','shared','mixed','personal'}:
                raise UpdateError('Unknown setting policy: ' + name)
    return lock


def sections(data):
    """GT's Configuration YAML permits unquoted ':'/'-' scalars; keep its text intact."""
    out, key = {}, None
    for line in data.decode('utf-8-sig').splitlines(keepends=True):
        match = re.fullmatch(r'([A-Za-z_][A-Za-z_0-9]*):\s*(?:#.*)?\n?', line)
        if match:
            key = match[1]
            if key in out:
                raise UpdateError('Duplicate configuration section: ' + key)
            out[key] = line
        elif key is not None:
            if line.strip() and not line[0].isspace() and not line.lstrip().startswith('#'):
                raise UpdateError('Unsupported top-level GT configuration syntax')
            out[key] += line
        elif line.strip() and not line.lstrip().startswith('#'):
            raise UpdateError('Unsupported GT configuration preamble')
    if not out:
        raise UpdateError('No configuration sections found')
    return out


def owned_view(data, policy):
    fmt = policy.get('format', 'bytes')
    if fmt == 'gt-sections':
        parsed = sections(data)
        personal = policy.get('personalSections', [])
        return {k:[line.rstrip() for line in v.splitlines() if line.strip() and not line.lstrip().startswith('#')]
                for k,v in parsed.items() if k not in personal}
    if fmt == 'json':
        parsed = json.loads(data)
        if policy['kind'] == 'mixed':
            if not isinstance(parsed, dict):
                raise UpdateError('Mixed JSON config must be an object')
            return {k:parsed[k] for k in policy['ownedKeys'] if k in parsed}
        return parsed
    if fmt == 'toml':
        return tomllib.loads(data.decode('utf-8-sig'))
    if fmt == 'yaml':
        import yaml
        return yaml.safe_load(data)
    return data.hex()


def owned_hash(data, policy):
    return sha(encoded(owned_view(data, policy)))


def merged_config(current, desired, policy):
    if policy['kind'] != 'mixed' or current is None:
        return desired
    if policy['format'] == 'gt-sections':
        old, new = sections(current), sections(desired)
        for key in policy['personalSections']:
            if key in old:
                new[key] = old[key]
        return ''.join(new.values()).encode()
    if policy['format'] == 'json':
        old, new = json.loads(current), json.loads(desired)
        for key in policy['ownedKeys']:
            if key in new:
                old[key] = new[key]
            else:
                old.pop(key, None)
        return (json.dumps(old, indent=2, ensure_ascii=False) + '\n').encode()
    raise UpdateError('Unsupported mixed configuration format')


def read_bundle(path):
    with zipfile.ZipFile(path) as archive:
        names, total = set(), 0
        for info in archive.infolist():
            relative(info.filename)
            if info.is_dir() or info.filename.casefold() in names or (info.external_attr >> 16) & 0o170000 == 0o120000:
                raise UpdateError('Invalid or duplicate update archive member')
            names.add(info.filename.casefold())
            total += info.file_size
        if total > MAX_PAYLOAD or len(names) > 20000:
            raise UpdateError('Update archive exceeds limits')
        spec = json.loads(archive.read('update-manifest.json'))
        if spec.get('schemaVersion') != 2:
            raise UpdateError('Unsupported update bundle')
        validate_lock(spec['base'])
        validate_lock(spec['target'])
        if spec.get('side') not in ('client', 'server'):
            raise UpdateError('Invalid update side')
        expected = {'update-manifest.json'} | {'payload/' + n for n,r in spec['sources'].items() if 'member' in r}
        if set(archive.namelist()) != expected:
            raise UpdateError('Unexpected or missing update archive contents')
        payload = {}
        for name, source in spec['sources'].items():
            target_path(Path(tempfile.gettempdir()) / 'gregstead-validation-root', name)
            desired = spec['target']['files'][spec['side']].get(name)
            if not desired:
                raise UpdateError('Source outside target release: ' + name)
            if 'member' in source:
                if source['member'] != 'payload/' + name:
                    raise UpdateError('Incorrect update member')
                data = archive.read(source['member'])
                if sha(data) != desired['sha256']:
                    raise UpdateError('Corrupt update payload: ' + name)
                payload[name] = data
            else:
                url = urllib.parse.urlsplit(source.get('url',''))
                if url.scheme != 'https' or not url.netloc or url.username or url.password or url.fragment:
                    raise UpdateError('Invalid official download URL')
        required = {n for n,r in spec['target']['files'][spec['side']].items()
                    if spec['base']['files'][spec['side']].get(n, {}).get('sha256') != r['sha256']}
        if set(spec['sources']) != required:
            raise UpdateError('Update does not cover exactly the release delta')
        return spec, payload


def fetch_payload(spec, payload, progress=lambda message: None):
    result = dict(payload)
    for name,data in result.items():
        row = spec['target']['files'][spec['side']].get(name)
        if row is None or sha(data) != row['sha256']:
            raise UpdateError('Update payload checksum differs: ' + name)
    for name, source in spec['sources'].items():
        if name in result:
            continue
        progress('Downloading ' + Path(name).name)
        request = urllib.request.Request(source['url'], headers={'User-Agent':'Gregstead-Updater/1'})
        with urllib.request.urlopen(request, timeout=60) as response:
            final = urllib.parse.urlsplit(response.geturl())
            if final.scheme != 'https':
                raise UpdateError('Download redirected away from HTTPS')
            data = response.read(MAX_PAYLOAD + 1)
        if len(data) > MAX_PAYLOAD or sha(data) != spec['target']['files'][spec['side']][name]['sha256']:
            raise UpdateError('Downloaded file checksum differs: ' + name)
        result[name] = data
    return result


def running_game(root):
    import psutil
    wanted = os.path.normcase(str(Path(root).resolve()))
    for process in psutil.process_iter(['name','cwd','cmdline']):
        try:
            info = process.info
            if 'java' not in (info.get('name') or '').lower():
                continue
            if info.get('cwd') and os.path.normcase(str(Path(info['cwd']).resolve())) == wanted:
                return True
            args = info.get('cmdline') or []
            for flag in ('--gameDir', '--gameDirectory'):
                if flag in args and args.index(flag) + 1 < len(args):
                    if os.path.normcase(str(Path(args[args.index(flag)+1]).resolve())) == wanted:
                        return True
        except (psutil.NoSuchProcess, psutil.AccessDenied, OSError):
            continue
    return False


def inspect_profile(root, lock, side='client', allowed_extras=None):
    validate_lock(lock)
    root = Path(root).resolve()
    different, personal, hashes = [], [], {}
    for name, row in lock['files'][side].items():
        path = target_path(root, name)
        data = path.read_bytes() if path.is_file() else None
        hashes[name] = sha(data) if data is not None else None
        policy = row['policy']
        if policy['kind'] == 'personal':
            if hashes[name] != row['sha256']:
                personal.append(name)
        elif data is None or (owned_hash(data, policy) if policy['kind'] in {'mixed','shared'} else sha(data)) != row.get('ownedHash', row['sha256']):
            different.append(name)
    expected_mods = {n.removeprefix('mods/') for n in lock['files'][side] if n.startswith('mods/')}
    extras = sorted(p.name for p in (root/'mods').glob('*.jar') if p.name not in expected_mods)
    allowed_extras = allowed_extras or {}
    allowed = sorted(n for n in extras if isinstance(allowed_extras,dict)
                     and allowed_extras.get(n) == file_sha(root/'mods'/n))
    unknown = sorted(set(extras) - set(allowed))
    receipt = root / RECEIPT
    installed = json.loads(receipt.read_text()) if receipt.is_file() else None
    return {'version':lock['version'], 'fingerprint':lock['fingerprint'], 'differences':different,
            'personalOverrides':personal, 'extraMods':unknown, 'allowedHostExtras':allowed,
            'matches':not different and not unknown, 'managedMatches':not different,
            'receipt':installed, 'hashes':hashes}


def plan_update(root, spec, payload, observation=None, allowed_extras=None):
    validate_lock(spec['base'])
    validate_lock(spec['target'])
    root = Path(root).resolve()
    if observation is None and not root.is_dir():
        raise UpdateError('Profile folder does not exist')
    side = spec['side']
    base, target = spec['base']['files'][side], spec['target']['files'][side]
    receipt = root / RECEIPT
    current = observation.get('gregsteadReceipt') if observation is not None else (json.loads(receipt.read_text()) if receipt.is_file() else None)
    if current:
        if current.get('fingerprint') not in (spec['base']['fingerprint'], spec['target']['fingerprint']):
            raise UpdateError('This installer does not support the installed release')
    metadata = root/'minecraftinstance.json'
    cf = observation.get('minecraft',{}) if observation is not None else (json.loads(metadata.read_text()) if metadata.is_file() else {})
    if cf:
        if cf.get('gameVersion') not in (None, spec['target']['minecraft']):
            raise UpdateError('Minecraft version differs from this update')
        loader = cf.get('baseModLoader') or {}
        if isinstance(loader, dict) and loader.get('name') and loader['name'] != 'neoforge-' + spec['target']['neoforge']:
            raise UpdateError('NeoForge version differs; install the full CurseForge pack')
    expected, writes, removes, preserved, conflicts = {}, {}, [], [], []
    for name in sorted(set(base) | set(target)):
        path = target_path(root, name)
        if observation is None:
            present = path.read_bytes() if path.is_file() else None
            present_hash = sha(present) if present is not None else None
        else:
            present_hash = observation['files'].get(name)
            present = bytes.fromhex(observation['contents'][name]) if name in observation.get('contents',{}) else (b'' if present_hash is not None else None)
        expected[name] = present_hash
        before, after = base.get(name), target.get(name)
        policy = (after or before)['policy']
        if policy['kind'] == 'personal':
            preserved.append(name)
            if present is None and after is not None:
                if name in payload:
                    writes[name] = payload[name]
            continue
        if after is None:
            if present is None:
                continue
            matches = owned_hash(present, policy) == before.get('ownedHash') if policy['kind'] in {'shared','mixed'} else present_hash == before['sha256']
            if not matches:
                conflicts.append(name + ': modified file would be removed')
            elif policy['kind'] == 'mixed':
                conflicts.append(name + ': mixed settings removal needs an explicit migration')
            else:
                removes.append(name)
            continue
        equivalent = lambda row: (owned_hash(present, policy) == row.get('ownedHash') if policy['kind'] in {'shared','mixed'} else present_hash == row['sha256']) if present is not None else False
        if equivalent(after):
            continue
        if present is not None and (before is None or not equivalent(before)):
            conflicts.append(name + ': differs from both supported releases')
            continue
        if present is None and before is not None:
            conflicts.append(name + ': expected installed file is missing')
            continue
        if name not in payload:
            conflicts.append(name + ': unchanged file is missing or modified; repair the profile first')
            continue
        writes[name] = merged_config(present, payload[name], policy)
    # Never remove arbitrary mods, but don't claim a clean supported profile either.
    expected_mods = {n.removeprefix('mods/') for n in set(base)|set(target) if n.startswith('mods/')}
    installed_mods = observation.get('mods',[]) if observation is not None else [p.name for p in (root/'mods').glob('*.jar')]
    extras = sorted(n for n in installed_mods if n not in expected_mods)
    allowed_extras = allowed_extras or {}
    if observation is not None:
        extra_hashes = observation.get('modHashes',{})
    else:
        extra_hashes = {n:file_sha(root/'mods'/n) for n in extras}
    extras = [n for n in extras if not (side == 'server' and allowed_extras.get(n) == extra_hashes.get(n))]
    if extras:
        conflicts += ['Additional mod needs review: ' + n for n in extras]
    if conflicts:
        raise UpdateError('Update blocked before changes:\n' + '\n'.join(conflicts))
    if not any(n.startswith('mods/') for n in base):
        raise UpdateError('Installer requires an existing pack, not an empty profile')
    return {'root':str(root), 'fromVersion':spec['base']['version'], 'toVersion':spec['target']['version'],
            'expected':expected, 'writes':writes, 'remove':removes, 'preserved':preserved,
            'fingerprint':spec['target']['fingerprint'], 'side':side}


def _restore(root, backup, journal):
    for name, old in journal['before'].items():
        dest = root/RECEIPT if name == RECEIPT else target_path(root, name)
        if old is None:
            dest.unlink(missing_ok=True)
        else:
            data = (backup/'files'/name).read_bytes()
            if sha(data) != old['sha256']:
                raise UpdateError('Recovery backup checksum differs: ' + name)
            atomic(dest, data)
            os.chmod(dest, old['mode'])


def recover(root, operation):
    """Explicit recovery of a crashed update. Worlds are never in this journal."""
    import psutil
    root = Path(root).resolve()
    if not re.fullmatch('[a-f0-9]{32}', operation):
        raise UpdateError('Invalid recovery operation')
    lock = root/LOCK
    owner = json.loads(lock.read_text()) if lock.exists() else None
    if owner and owner.get('operation') != operation:
        raise UpdateError('The profile lock belongs to a different operation')
    if owner and (owner.get('host') != socket.gethostname() or psutil.pid_exists(owner['pid'])):
        raise UpdateError('Another updater may still be active')
    backup = root/'.gregstead-backups'/operation
    if backup.is_symlink() or not backup.resolve().is_relative_to(root):
        raise UpdateError('Invalid backup location')
    journal_path = backup/'journal.json'
    journal = json.loads(journal_path.read_text())
    if journal['phase'] not in ('prepared','complete','applying','recovery-required'):
        raise UpdateError('This operation does not need recovery')
    if running_game(root):
        raise UpdateError('Close Minecraft before recovery')
    if journal['phase'] in ('applying','recovery-required'):
        for name,old in journal['before'].items():
            dest = root/RECEIPT if name==RECEIPT else target_path(root,name)
            present = file_sha(dest) if dest.is_file() else None
            if name==RECEIPT:
                if present is not None and (old is None or present!=old['sha256']):
                    if json.loads(dest.read_text()).get('operation')!=operation:
                        raise UpdateError('Profile receipt advanced after this interrupted operation')
            elif 'after' in journal:
                allowed={old['sha256'] if old else None,journal['after'].get(name)}
                if present not in allowed:
                    raise UpdateError('File changed after interruption; inspect recovery manually: '+name)
        _restore(root, backup, journal)
        journal['phase'] = 'rolled-back'
    elif journal['phase']=='prepared':
        journal['phase'] = 'rolled-back'
    atomic(journal_path, encoded(journal))
    lock.unlink(missing_ok=True)
    return {'recovered':operation}


def apply_update(root, spec, payload, progress=lambda message: None):
    if spec['side'] != 'client':
        raise UpdateError('Use the workspace server installer with stopped-world backup')
    root = Path(root).resolve()
    if running_game(root):
        raise UpdateError('Close Minecraft before installing this update')
    lock = root/LOCK
    if lock.is_symlink() or (root/'.gregstead-backups').is_symlink() or (root/RECEIPT).is_symlink():
        raise UpdateError('Symlink in updater state')
    try:
        fd = os.open(lock, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    except FileExistsError:
        raise UpdateError('Another update or an interrupted update owns this profile; inspect recovery first') from None
    operation = uuid.uuid4().hex
    backup = root/'.gregstead-backups'/operation
    journal = None
    try:
        with os.fdopen(fd, 'wb') as stream:
            stream.write(encoded({'pid':os.getpid(), 'host':socket.gethostname(), 'operation':operation}))
        # Plan again under the lock, after all downloads. Reject stale UI observations.
        payload = fetch_payload(spec, payload, progress)
        plan = plan_update(root, spec, payload)
        if running_game(root):
            raise UpdateError('Minecraft was started while preparing the update')
        needed = sum(len(b) for b in plan['writes'].values()) * 2 + 64*1024*1024
        if shutil.disk_usage(root).free < needed:
            raise UpdateError('Insufficient free disk space for staging and backup')
        backup.mkdir(parents=True, mode=0o700)
        before = {}
        for name in [*sorted(set(plan['writes']) | set(plan['remove'])), RECEIPT]:
            path = root/RECEIPT if name == RECEIPT else target_path(root, name)
            if path.is_file():
                data = path.read_bytes()
                before[name] = {'sha256':sha(data), 'mode':path.stat().st_mode & 0o777}
                atomic(backup/'files'/name, data)
                if file_sha(backup/'files'/name) != before[name]['sha256']:
                    raise UpdateError('Backup verification failed: ' + name)
            else:
                before[name] = None
        journal = {'operation':operation, 'phase':'prepared', 'before':before,
                   'after':{**{n:sha(b) for n,b in plan['writes'].items()},**{n:None for n in plan['remove']}},
                   'fromVersion':plan['fromVersion'], 'toVersion':plan['toVersion']}
        atomic(backup/'journal.json', encoded(journal))
        for name, expected in plan['expected'].items():
            path = target_path(root, name)
            if (file_sha(path) if path.is_file() else None) != expected:
                raise UpdateError('Profile changed after planning: ' + name)
        journal['phase'] = 'applying'
        atomic(backup/'journal.json', encoded(journal))
        for name, data in plan['writes'].items():
            progress('Installing ' + Path(name).name)
            path = target_path(root, name)
            mode = path.stat().st_mode & 0o777 if path.exists() else 0o644
            atomic(path, data)
            os.chmod(path, mode)
        for name in plan['remove']:
            target_path(root, name).unlink()
        report = inspect_profile(root, spec['target'], spec['side'])
        if not report['managedMatches']:
            raise UpdateError('Installed release verification failed')
        receipt = {'schemaVersion':1, 'version':plan['toVersion'], 'previousVersion':plan['fromVersion'],
                   'fingerprint':plan['fingerprint'], 'sourceCommit':spec['target']['sourceCommit'],
                   'operation':operation, 'completedAt':time.time(), 'side':spec['side'],
                   'health':'installed-not-launched', 'personalOverrides':report['personalOverrides']}
        atomic(root/RECEIPT, encoded(receipt))
        journal['phase'] = 'complete'
        atomic(backup/'journal.json', encoded(journal))
        return receipt
    except BaseException:
        if journal and journal['phase'] == 'applying':
            try:
                _restore(root, backup, journal)
                journal['phase'] = 'rolled-back'
            except BaseException:
                journal['phase'] = 'recovery-required'
                atomic(backup/'journal.json', encoded(journal))
                raise UpdateError('Update interrupted; verified backup needs recovery: ' + operation) from None
            atomic(backup/'journal.json', encoded(journal))
        raise
    finally:
        if not journal or journal['phase'] != 'recovery-required':
            lock.unlink(missing_ok=True)
