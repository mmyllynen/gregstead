#!/usr/bin/env python3
"""Generate 26.3 sign resources from pinned vanilla layouts and Gregstead wood palettes.

Run after game datagen. Normal builds consume the checked-in generated resources.
Requires Pillow and the verified .build/minecraft/client.jar input.
"""
import hashlib
import sys
import io
import json
from pathlib import Path
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'core/sources/GregTech-Modern/src/generated/resources/assets/gtceu'
SOURCE = ROOT / 'core/sources/GregTech-Modern/src/main/resources/assets/gtceu'


def main():
    sys.path.insert(0, str(ROOT))
    from release import MINECRAFT_JSON_SHA1
    metadata = (ROOT / '.build/minecraft/26.3-official.json').read_bytes()
    if hashlib.sha1(metadata).hexdigest() != MINECRAFT_JSON_SHA1:
        raise RuntimeError('Unverified Minecraft metadata')
    client = ROOT / '.build/minecraft/client.jar'
    if hashlib.sha1(client.read_bytes()).hexdigest() != json.loads(metadata)['downloads']['client']['sha1']:
        raise RuntimeError('Unverified Minecraft client assets')
    with zipfile.ZipFile(ROOT / '.build/minecraft/client.jar') as archive:
        oak = Image.open(io.BytesIO(archive.read('assets/minecraft/textures/block/oak_planks.png'))).convert('RGB')
        oak_colors = list(oak.getdata())
        for wood in ['rubber', 'treated_wood']:
            plank = Image.open(SOURCE / ('textures/block/' + wood + '_planks.png')).convert('RGB')
            replacement = [plank.getpixel((i % oak.width % plank.width, i // oak.width % plank.height)) for i in range(len(oak_colors))]
            for folder, name in [('block', 'oak_sign'), ('block', 'oak_hanging_sign'), ('gui/signs', 'oak'), ('gui/hanging_signs', 'oak')]:
                image = Image.open(io.BytesIO(archive.read(f'assets/minecraft/textures/{folder}/{name}.png'))).convert('RGBA')
                pixels = []
                for r, g, b, a in image.getdata():
                    if a == 0 or (folder.endswith('hanging_signs') or 'hanging' in name) and max(r,g,b)-min(r,g,b)<12:
                        pixels.append((r,g,b,a));continue
                    index = min(range(len(oak_colors)), key=lambda i: sum((v-w)**2 for v,w in zip((r,g,b),oak_colors[i])))
                    pixels.append((*replacement[index],a))
                image.putdata(pixels)
                filename = wood + ('_hanging_sign' if name=='oak_hanging_sign' else '_sign' if name=='oak_sign' else '')
                dest=SOURCE / f'textures/{folder}/{filename}.png';dest.parent.mkdir(parents=True,exist_ok=True);image.save(dest)
            for name in archive.namelist():
                if not name.endswith('.json'):continue
                if not (name.startswith('assets/minecraft/blockstates/oak_') or name.startswith('assets/minecraft/models/block/oak_')):continue
                stem=Path(name).stem
                if not any(stem==s or stem.startswith(s+'_') for s in ['oak_sign','oak_wall_sign','oak_hanging_sign','oak_wall_hanging_sign']):continue
                data=archive.read(name).decode()
                for old,new in [('minecraft:block/oak_planks',f'gtceu:block/{wood}_planks'),('minecraft:block/oak_',f'gtceu:block/{wood}_')]:data=data.replace(old,new)
                rel=name.removeprefix('assets/minecraft/').replace('/oak_',f'/{wood}_')
                dest=ASSETS/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(json.loads(data),indent=2)+'\n')


if __name__ == '__main__':main()
