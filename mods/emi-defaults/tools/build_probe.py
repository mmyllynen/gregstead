#!/usr/bin/env python3
"""Build a test-only export/verification probe; never part of the production JAR."""
from pathlib import Path
import argparse, subprocess, tempfile, zipfile


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('kind', choices=['export', 'verify'])
    p.add_argument('--classpath-file', type=Path, required=True)
    p.add_argument('--output', type=Path, required=True)
    args = p.parse_args()
    tools = Path(__file__).resolve().parent
    root = tools.parents[2]
    name = 'RecipeExportProbe' if args.kind == 'export' else 'VerifyDefaultsProbe'
    mod = 'neogreg_recipe_export' if args.kind == 'export' else 'neogreg_defaults_verify'
    with tempfile.TemporaryDirectory(prefix='neogreg-emi-probe-') as temp:
        classes = Path(temp) / 'classes'
        subprocess.run([str(root / 'core/toolchains/jdk-25/bin/javac'), '-proc:none', '-cp', args.classpath_file.read_text().strip(), '-d', str(classes), str(tools / (name + '.java'))], check=True)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(args.output, 'w', zipfile.ZIP_DEFLATED) as z:
            z.writestr('META-INF/neoforge.mods.toml', f'modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="{mod}"\nversion="1"\n')
            for entry in sorted(classes.rglob('*.class')):
                z.write(entry, entry.relative_to(classes))


if __name__ == '__main__':
    main()
