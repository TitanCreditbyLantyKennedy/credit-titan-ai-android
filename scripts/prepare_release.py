#!/usr/bin/env python3
"""Load secret material without logging it; require an independently pinned baseline."""
import base64
import hashlib
import os
from pathlib import Path
import urllib.request
from urllib.parse import urlparse
from verify_apk import fingerprint, inspect


def main():
    url = os.environ['PREVIOUS_APK_URL']
    if urlparse(url).scheme != 'https':
        raise ValueError('Previous APK must have an HTTPS URL')
    digest = fingerprint(os.environ['PREVIOUS_APK_SHA256'])
    cert = fingerprint(os.environ['RELEASE_CERT_SHA256'])
    directory = Path('.validation')
    directory.mkdir(exist_ok=True)
    with urllib.request.urlopen(url, timeout=60) as response:
        if urlparse(response.url).scheme != 'https':
            raise ValueError('Baseline redirected to an insecure URL')
        content = response.read(100 * 1024 * 1024 + 1)
    if len(content) > 100 * 1024 * 1024 or hashlib.sha256(content).hexdigest() != digest:
        raise ValueError('Previous APK size/hash check failed')
    previous = directory / 'previous.apk'
    previous.write_bytes(content)
    if inspect(previous)['certificate_sha256'] != cert:
        raise ValueError('Previous APK does not match the approved release identity')
    key = Path(os.environ['RUNNER_TEMP']) / 'credit-titan-release.jks'
    # Refuse replacement and restrict access before writing key bytes.
    with os.fdopen(os.open(key, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600), 'wb') as handle:
        handle.write(base64.b64decode(os.environ['KEYSTORE_BASE64'], validate=True))


if __name__ == '__main__':
    main()
