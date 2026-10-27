"""Bounded CurseForge profile discovery; never recursively scan a user's home."""
import json
import os
from pathlib import Path
from updater import core


def roots(home=None, platform=None):
    home = Path(home or Path.home())
    platform = platform or os.name
    candidates = [home/'curseforge/minecraft/Instances', home/'Documents/curseforge/minecraft/Instances',
                  home/'Documents/CurseForge/Minecraft/Instances']
    if platform == 'nt':
        appdata = Path(os.environ.get('APPDATA', home/'AppData/Roaming'))
        configs = [appdata/'CurseForge/storage.json', appdata/'CurseForge/settings.json', appdata/'CurseForge/Settings.json']
    else:
        config = Path(os.environ.get('XDG_CONFIG_HOME',home/'.config'))
        configs = [config/'CurseForge/storage.json', config/'CurseForge/settings.json', config/'curseforge/storage.json', config/'curseforge/settings.json']
    # Only documented-by-observation installation paths; unrelated app data is ignored.
    for path in configs:
        try:
            data = json.loads(path.read_text(encoding='utf-8-sig'))
            minecraft = data.get('minecraft-settings',data.get('minecraft', {}))
            if isinstance(minecraft,str):
                minecraft = json.loads(minecraft)
            if isinstance(minecraft,dict) and isinstance(minecraft.get('minecraftRoot'),str):
                candidates.append(Path(os.path.expandvars(minecraft['minecraftRoot'])).expanduser()/'Instances')
            for key in ('installPath','moddingFolder','gameInstancesPath'):
                value = data.get(key) or (minecraft.get(key) if isinstance(minecraft,dict) else None)
                if isinstance(value,str):
                    p = Path(os.path.expandvars(value)).expanduser()
                    candidates.extend([p, p/'Instances'])
        except (OSError,ValueError):
            pass
    return list(dict.fromkeys(p.resolve() for p in candidates if p.is_dir()))


def identify(path, base=None):
    path = Path(path)
    receipt = path/core.RECEIPT
    try:
        if receipt.is_file() and not receipt.is_symlink():
            d = json.loads(receipt.read_text(encoding='utf-8-sig'))
            if d.get('fingerprint') and d.get('version'):
                return {'path':str(path.resolve()),'name':path.name,'version':d['version'],'via':'receipt'}
        cf_path = path/'minecraftinstance.json'
        cf = json.loads(cf_path.read_text(encoding='utf-8-sig')) if cf_path.is_file() else {}
        pack = cf.get('installedModpack') or {}
        if isinstance(pack,dict) and pack.get('addonID') == core.PROJECT_ID:
            return {'path':str(path.resolve()),'name':cf.get('name',path.name),
                    'version':str(pack.get('version') or pack.get('fileID') or 'CurseForge'), 'via':'curseforge'}
        if base:
            mods = {n:r for n,r in base['files']['client'].items() if n.startswith('mods/')}
            if mods and all((path/n).is_file() and core.file_sha(path/n) == r['sha256'] for n,r in mods.items()):
                return {'path':str(path.resolve()),'name':cf.get('name',path.name), 'version':base['version'],'via':'verified-base'}
    except (OSError,ValueError):
        pass
    return None


def discover(base=None, search_roots=None):
    found = []
    for root in search_roots if search_roots is not None else roots():
        for path in sorted(Path(root).iterdir()):
            if path.is_dir() and not path.is_symlink():
                result = identify(path, base)
                if result:
                    found.append(result)
    return list({r['path']:r for r in found}.values())
