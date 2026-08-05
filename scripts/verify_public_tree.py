#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
forbidden_suffixes = {
    '.apk', '.aab', '.aar', '.onnx', '.tflite', '.wav', '.mp3', '.m4a',
    '.db', '.sqlite', '.sqlite3', '.jks', '.keystore', '.p12'
}
forbidden_names = {'.env', '.env.local', 'local.properties', 'google-services.json'}
secret_patterns = [
    re.compile(r'(?:ghp|github_pat)_[A-Za-z0-9_]{20,}'),
    re.compile(r'sk-(?:proj-)?[A-Za-z0-9_-]{20,}'),
    re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----'),
    re.compile(r'AKIA[0-9A-Z]{16}'),
]
errors: list[str] = []
for path in ROOT.rglob('*'):
    if '.git' in path.parts or 'build' in path.parts or '.gradle' in path.parts:
        continue
    if path.is_file() and (path.name in forbidden_names or path.suffix.lower() in forbidden_suffixes):
        errors.append(f'forbidden file: {path.relative_to(ROOT)}')
    if path.is_file() and path.stat().st_size <= 2_000_000:
        try:
            text = path.read_text(encoding='utf-8')
        except UnicodeDecodeError:
            continue
        for pattern in secret_patterns:
            if pattern.search(text):
                errors.append(f'secret pattern in: {path.relative_to(ROOT)}')
if errors:
    print('\n'.join(errors))
    sys.exit(1)
print('PUBLIC_TREE_OK')
