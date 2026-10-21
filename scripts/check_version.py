#!/usr/bin/env python3
"""Reject unbumped app versions on PRs, including migration from the legacy Gradle literal."""
import argparse
from pathlib import Path
import re
import subprocess


def version(text):
    match = re.search(r'^versionCode=([1-9][0-9]*)$', text, re.M)
    if not match:
        match = re.search(r'\bversionCode\s+([1-9][0-9]*)\b', text)
    if not match or int(match[1]) > 2100000000:
        raise ValueError('Missing or invalid versionCode')
    return int(match[1])


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--base-ref', required=True)
    args = p.parse_args()
    changed = subprocess.check_output(['git', 'diff', '--name-only', args.base_ref, 'HEAD'], text=True).splitlines()
    current = version(Path('version.properties').read_text())
    if any(path.startswith('app/') or path in ('build.gradle', 'version.properties') for path in changed):
        old = subprocess.run(['git', 'show', args.base_ref + ':version.properties'], text=True, capture_output=True)
        baseline = old.stdout if old.returncode == 0 else subprocess.check_output(
            ['git', 'show', args.base_ref + ':app/build.gradle'], text=True)
        if current <= version(baseline):
            raise ValueError('App changes require versionCode above the target branch')
    print('Source version policy passed:', current)


if __name__ == '__main__':
    main()
