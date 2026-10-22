#!/usr/bin/env python3
"""Run only on a disposable emulator. Never uninstalls or clears app data."""
import argparse
import subprocess
import time
from verify_apk import PACKAGE, ACTIVITY, inspect


def adb(*args):
    result = subprocess.run(['adb', *args], text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    return result.returncode, result.stdout


def install(path):
    code, output = adb('install', '-r', path)
    if code or 'Success' not in output or 'Failure' in output:
        raise AssertionError(output)


def probe(phase):
    code, output = adb('shell', 'am', 'instrument', '-w', '-e', 'phase', phase,
                       PACKAGE + '.test/com.credittitan.ai.UpgradeProbe')
    if code or 'result=PASS' not in output or 'INSTRUMENTATION_CODE: -1' not in output:
        raise AssertionError(output)


def launch():
    code, output = adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/' + ACTIVITY)
    if code or 'Status: ok' not in output or 'Error' in output:
        raise AssertionError(output)
    time.sleep(2)
    code, pid = adb('shell', 'pidof', PACKAGE)
    if code or not pid.strip():
        raise AssertionError('App process did not survive launch')
    _, activities = adb('shell', 'dumpsys', 'activity', 'activities')
    if not any(PACKAGE in line and ('mResumedActivity' in line or 'topResumedActivity' in line)
               for line in activities.splitlines()):
        raise AssertionError('App activity is not resumed')


def reject(path, reason, expected_version):
    _, output = adb('install', '-r', path)
    if 'Failure' not in output or reason not in output:
        raise AssertionError('Expected ' + reason + ': ' + output)
    _, package = adb('shell', 'dumpsys', 'package', PACKAGE)
    if 'versionCode=' + str(expected_version) + ' ' not in package:
        raise AssertionError('Rejected update changed installed version')
    probe('verify')
    launch()


def main():
    p = argparse.ArgumentParser()
    for name in ('previous', 'candidate', 'mismatch', 'probe'):
        p.add_argument('--' + name, required=True)
    p.add_argument('--font-scale', choices=['1.0', '1.3'], default='1.0')
    args = p.parse_args()
    _, emulator = adb('shell', 'getprop', 'ro.kernel.qemu')
    if emulator.strip() != '1':
        raise AssertionError('Tests require a disposable Android emulator; refusing a physical device')
    old, new, wrong = inspect(args.previous), inspect(args.candidate), inspect(args.mismatch)
    if not old['version_code'] < new['version_code'] == wrong['version_code']:
        raise AssertionError('Test APK versions are invalid')
    if old['certificate_sha256'] != new['certificate_sha256'] or new['certificate_sha256'] == wrong['certificate_sha256']:
        raise AssertionError('Test APK certificate setup is invalid')
    _, existing = adb('shell', 'pm', 'path', PACKAGE)
    if 'package:' in existing:
        raise AssertionError('Requires a fresh disposable emulator; will not uninstall')
    code, output = adb('shell', 'settings', 'put', 'system', 'font_scale', args.font_scale)
    if code:
        raise AssertionError(output)
    install(args.previous)
    install(args.probe)
    probe('seed')
    launch()
    install(args.candidate)
    probe('verify')
    launch()
    reject(args.previous, 'INSTALL_FAILED_VERSION_DOWNGRADE', new['version_code'])
    reject(args.mismatch, 'INSTALL_FAILED_UPDATE_INCOMPATIBLE', new['version_code'])
    probe('ui')
    print('PASS: UI navigation, notes, bureau drafts, unsaved editor recreation, calculator, checklist, privacy cancel')
    print('PASS: fresh install, launch, in-place upgrade, saved notes/draft/checklists, downgrade and wrong-key rejection')


if __name__ == '__main__':
    main()
