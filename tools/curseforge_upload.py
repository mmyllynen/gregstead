#!/usr/bin/env python3
"""Upload an immutable release file through CurseForge's documented author API.

Token is read from KWallet into memory, never a command argument, URL or log.
No automatic POST retries: inspect the receipt before retrying an uncertain upload.
"""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import requests


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--project-id', type=int, required=True)
    parser.add_argument('--file', type=Path, required=True)
    parser.add_argument('--display-name', required=True)
    parser.add_argument('--changelog-file', type=Path, required=True)
    parser.add_argument('--parent-file-id', type=int)
    parser.add_argument('--environment', action='append', choices=['Client', 'Server'])
    parser.add_argument('--release-type', choices=['alpha', 'beta', 'release'], default='beta')
    parser.add_argument('--receipt', type=Path, required=True)
    args = parser.parse_args()
    if args.receipt.exists():
        raise SystemExit('Receipt already exists; inspect it before uploading again')
    checksum = hashlib.sha256(args.file.read_bytes()).hexdigest()
    metadata = dict(displayName=args.display_name, changelog=args.changelog_file.read_text(),
                    changelogType='markdown', releaseType=args.release_type)
    if args.parent_file_id:
        metadata['parentFileID'] = args.parent_file_id
    else:
        metadata['gameVersionNames'] = ['26.3', 'NeoForge', 'Java 25'] + (args.environment or ['Client'])
    result = subprocess.run(['kwallet-query', '-f', 'CurseForge', '-r', 'API Token', 'kdewallet'],
                            capture_output=True, text=True)
    if result.returncode or not result.stdout.strip():
        raise SystemExit('KWallet token unavailable (secret output suppressed)')
    token = result.stdout.strip()
    try:
        with args.file.open('rb') as stream:
            response = requests.post(
                f'https://minecraft.curseforge.com/api/projects/{args.project_id}/upload-file',
                headers={'X-Api-Token': token}, data={'metadata': json.dumps(metadata)},
                files={'file': (args.file.name, stream, 'application/octet-stream')},
                timeout=(30, 300), allow_redirects=False)
    except requests.RequestException as exc:
        raise SystemExit(f'Upload outcome uncertain ({type(exc).__name__}); check author dashboard before retrying') from None
    if response.status_code not in (200, 201):
        # Do not expose reflected credentials or raw response headers/body.
        message = response.text.replace(token, '[redacted]')[:1200]
        raise SystemExit(f'Upload HTTP {response.status_code}: {message}')
    try:
        file_id = int(response.json()['id'])
    except (ValueError, KeyError, TypeError):
        raise SystemExit('Upload response missing file ID; check dashboard before retrying') from None
    receipt = dict(projectID=args.project_id, fileID=file_id, filename=args.file.name,
                   sha256=checksum, metadata=metadata, status='Submitted; approval not yet verified')
    args.receipt.parent.mkdir(parents=True, exist_ok=True)
    args.receipt.write_text(json.dumps(receipt, indent=2) + '\n')
    print(json.dumps({k: receipt[k] for k in ['projectID', 'fileID', 'filename', 'sha256', 'status']}))


if __name__ == '__main__':
    main()
