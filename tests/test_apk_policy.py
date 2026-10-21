import sys
from pathlib import Path
import unittest
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'scripts'))
from verify_apk import compare, fingerprint, parse_badging


class UpdatePolicy(unittest.TestCase):
    def test_valid_upgrade(self):
        compare(dict(version_code=2, certificate_sha256='a'*64),
                dict(version_code=3, certificate_sha256='a'*64), 'AA:'*31+'AA')

    def test_reused_and_lower_version_rejected(self):
        for code in (1, 2):
            with self.assertRaises(ValueError):
                compare(dict(version_code=2, certificate_sha256='a'*64),
                        dict(version_code=code, certificate_sha256='a'*64), 'a'*64)

    def test_both_certificates_must_match_pin(self):
        for old, new in [('b', 'a'), ('a', 'b'), ('b', 'b')]:
            with self.assertRaises(ValueError):
                compare(dict(version_code=2, certificate_sha256=old*64),
                        dict(version_code=3, certificate_sha256=new*64), 'a'*64)

    def test_badging_requires_launcher(self):
        with self.assertRaises(ValueError):
            parse_badging("package: name='com.credittitanai.app' versionCode='2'")
        self.assertEqual(parse_badging("package: name='com.credittitanai.app' versionCode='3'\nlaunchable-activity: name='com.credittitan.ai.MainActivity'"),
                         ('com.credittitanai.app', 3, 'com.credittitan.ai.MainActivity'))

    def test_invalid_fingerprint_rejected(self):
        for value in ('', 'aa', 'z'*64):
            with self.assertRaises(ValueError):
                fingerprint(value)

class BuiltApkChecks(unittest.TestCase):
    def setUp(self):
        import tempfile
        import zipfile
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.apk = Path(self.tmp.name) / 'test.apk'
        with zipfile.ZipFile(self.apk, 'w') as archive:
            archive.writestr('AndroidManifest.xml', b'manifest')
            archive.writestr('classes.dex', b'dex')
        self.badging = "package: name='com.credittitanai.app' versionCode='3'\nlaunchable-activity: name='com.credittitan.ai.MainActivity'"

    def inspect_with(self, certs=None, badging=None):
        from unittest.mock import patch
        from verify_apk import inspect
        certs = certs if certs is not None else 'Signer #1 certificate SHA-256 digest: ' + 'a'*64
        with patch.dict('os.environ', {'ANDROID_HOME': '/test-sdk'}), \
                patch('verify_apk.run', side_effect=[certs, self.badging if badging is None else badging]):
            return inspect(self.apk)

    def test_verified_apk(self):
        self.assertEqual(self.inspect_with()['version_code'], 3)
        self.assertEqual(self.inspect_with('V2 Signer: certificate SHA-256 digest: ' + 'a'*64)['certificate_sha256'], 'a'*64)

    def test_wrong_identity_and_debuggable(self):
        for text in (self.badging.replace('com.credittitanai.app', 'com.other.app'),
                     self.badging.replace('com.credittitan.ai.MainActivity', 'com.other.Activity'),
                     self.badging + '\napplication-debuggable'):
            with self.assertRaises(ValueError):
                self.inspect_with(badging=text)

    def test_unsigned_multiple_and_debug_certificates(self):
        for certs in ('', 'Signer #1 certificate SHA-256 digest: ' + 'a'*64 + '\nSigner #2 certificate SHA-256 digest: ' + 'b'*64,
                      'Signer #1 certificate SHA-256 digest: ' + 'a'*64 + '\nSigner #1 certificate DN: CN=Android Debug, O=Android'):
            with self.assertRaises(ValueError):
                self.inspect_with(certs)

    def test_invalid_zip_rejected_before_sdk(self):
        import zipfile
        self.apk.write_bytes(b'not an apk')
        with self.assertRaises(zipfile.BadZipFile):
            self.inspect_with()


if __name__ == '__main__':
    unittest.main()
