package com.credittitan.ai;

import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

/** Test APK only: exercises the real preference file without shipping test hooks. */
public class UpgradeProbe extends Instrumentation {
    private Bundle args;
    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        args = arguments;
        start();
    }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            Context target = getTargetContext();
            if (!"com.credittitanai.app".equals(target.getPackageName())) {
                throw new AssertionError("Wrong target package");
            }
            SharedPreferences prefs = target.getSharedPreferences("titan_local", Context.MODE_PRIVATE);
            if ("seed".equals(args.getString("phase"))) {
                if (!prefs.edit().putString("report_notes", "upgrade-test-report")
                        .putString("dispute_draft", "upgrade-test-draft")
                        .putBoolean("boost0", true).putBoolean("goal0", true).commit()) {
                    throw new AssertionError("Unable to seed preferences");
                }
                java.io.File directory = new java.io.File(target.getFilesDir(), "reports");
                if (!directory.isDirectory() && !directory.mkdirs()) throw new AssertionError("Cannot seed report directory");
                try (java.io.FileOutputStream out = new java.io.FileOutputStream(new java.io.File(directory, "upgrade-report.pdf"))) {
                    out.write("%PDF-1.7\nupgrade-test-only\n%%EOF".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            } else if ("verify".equals(args.getString("phase"))) {
                if (!"upgrade-test-report".equals(prefs.getString("report_notes", ""))
                        || !"upgrade-test-draft".equals(prefs.getString("dispute_draft", ""))
                        || !prefs.getBoolean("boost0", false) || !prefs.getBoolean("goal0", false)) {
                    throw new AssertionError("Saved user data changed during update");
                }
                java.io.File report = new java.io.File(target.getFilesDir(), "reports/upgrade-report.pdf");
                try (java.io.FileInputStream in = new java.io.FileInputStream(report)) {
                    byte[] contents = new byte[(int)report.length()];
                    if (in.read(contents) != contents.length || !new String(contents, java.nio.charset.StandardCharsets.UTF_8).contains("upgrade-test-only")) throw new AssertionError("Imported report changed during update");
                }
            } else if ("ui".equals(args.getString("phase"))) {
                new UiChecks(this).run();
            } else {
                throw new AssertionError("Unknown phase");
            }
            result.putString("result", "PASS");
            finish(-1, result);
        } catch (Throwable failure) {
            result.putString("result", "FAIL: " + failure);
            finish(0, result);
        }
    }
}
