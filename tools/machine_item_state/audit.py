#!/usr/bin/env python3
"""Inventory the source's explicit world/item persistence decisions for review."""
import json
import re
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'core/sources/GregTech-Modern/src/main/java/com/gregtechceu/gtceu'
rows = []
for path in sorted(SOURCE.rglob('*.java')):
    source = path.read_text()
    for match in re.finditer(r'@SaveField(?:\([^)]*\))?', source):
        tail = re.sub(r'/\*.*?\*/|//[^\n]*', '', source[match.end():], flags=re.S)
        tail = re.sub(r'@\w+(?:\([^)]*\))?', '', tail).strip()
        declaration = re.match(r'(?:(?:public|protected|private|final|volatile|static)\s+)*[\w.<>?,\[\] ]+?\s+(\w+(?:\s*,\s*\w+)*)\s*(?:=|;)', tail)
        if not declaration:
            raise SystemExit(f'Unrecognized declaration: {path}:{source[:match.start()].count(chr(10))+1}')
        policy = re.search(r'ItemSave\.(\w+)', match.group())
        rows.append({'class': str(path.relative_to(SOURCE)), 'fields': [f.strip() for f in declaration[1].split(',')], 'policy': policy[1] if policy else 'CHILDREN'})
output = Path(__file__).with_name('field-audit.json')
output.write_text(json.dumps(rows, indent=2) + '\n')
print(f'{len(rows)} declarations; {sum(r["policy"] in ("VALUE", "GHOST_CIRCUIT") for r in rows)} explicit portable/conditional values')
