#!/usr/bin/env python3
"""Fail-closed APK checks, using Android SDK build-tools 35.0.0."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import zipfile

PACKAGE = 'com.credittitanai.app'
ACTIVITY = 'com.credittitan.ai.MainActivity'


def run(*args):
    return subprocess.check_output(args, text=True, stderr=subprocess.STDOUT)


def fingerprint(value):
    value = value.replace(':', '').strip().lower()
    if not re.fullmatch('[0-9a-f]{64}', value):
        raise ValueError('Expected a SHA-256 certificate fingerprint')
    return value


def parse_badging(text):
    package = re.search(r"^package: name='([^']+)' versionCode='([0-9]+)'", text, re.M)
    activity = re.search(r"^launchable-activity: name='([^']+)'", text, re.M)
    if not package or not activity:
        raise ValueError('Missing package/version or launchable activity')
    return package[1], int(package[2]), activity[1]


def inspect(apk):
    apk = Path(apk)
    with zipfile.ZipFile(apk) as archive:
        if archive.testzip() or not {'AndroidManifest.xml', 'classes.dex'} <= set(archive.namelist()):
            raise ValueError('Corrupt or incomplete APK')
    sdk = os.environ.get('ANDROID_HOME') or os.environ['ANDROID_SDK_ROOT']
    build_tools = Path(sdk) / 'build-tools' / '35.0.0'
    certs = run(str(build_tools / 'apksigner'), 'verify', '--verbose', '--print-certs', str(apk))
    signers = set(re.findall(r'^.*certificate SHA-256 digest: ([0-9a-fA-F]{64})$', certs, re.M))
    if len(signers) != 1:
        raise ValueError('Require exactly one verified signing certificate')
    badging = run(str(build_tools / 'aapt'), 'dump', 'badging', str(apk))
    package, code, activity = parse_badging(badging)
    if package != PACKAGE or activity != ACTIVITY or not 1 <= code <= 2100000000:
        raise ValueError('Unexpected package, launcher, or version code')
    if 'application-debuggable' in badging:
        raise ValueError('Release must not be debuggable')
    if re.search(r'certificate DN:.*CN=Android Debug', certs, re.I):
        raise ValueError('Android debug signing is not a release identity')
    return dict(package=package, version_code=code, activity=activity,
                certificate_sha256=fingerprint(next(iter(signers))), sha256=hashlib.sha256(apk.read_bytes()).hexdigest())


def compare(previous, candidate, expected_cert):
    expected_cert = fingerprint(expected_cert)
    if previous['certificate_sha256'] != expected_cert or candidate['certificate_sha256'] != expected_cert:
        raise ValueError('Certificate continuity check failed; do not uninstall or distribute')
    if candidate['version_code'] <= previous['version_code']:
        raise ValueError('Candidate versionCode must be strictly greater than previous APK')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('candidate')
    parser.add_argument('--previous', required=True)
    parser.add_argument('--certificate', required=True)
    parser.add_argument('--version-code', required=True, type=int)
    args = parser.parse_args()
    candidate, previous = inspect(args.candidate), inspect(args.previous)
    compare(previous, candidate, args.certificate)
    if candidate['version_code'] != args.version_code:
        raise ValueError('APK version does not match requested source version')
    print(json.dumps(dict(previous=previous, candidate=candidate), indent=2))


if __name__ == '__main__':
    main()
