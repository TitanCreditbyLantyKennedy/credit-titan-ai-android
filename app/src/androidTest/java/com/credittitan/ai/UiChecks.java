package com.credittitan.ai;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.File;
import java.io.FileOutputStream;

/** Runs only in the test APK, on a disposable emulator, through accessibility actions. */
final class UiChecks {
    private final Instrumentation test;
    UiChecks(Instrumentation test) { this.test = test; }
    private AccessibilityNodeInfo find(String text) {
        for (int attempt = 0; attempt < 40; attempt++) {
            test.waitForIdleSync();
            AccessibilityNodeInfo root = root();
            if (root != null) {
                for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(text)) {
                    if (text.equalsIgnoreCase(node.getText() == null ? "" : node.getText().toString()) && node.isVisibleToUser()) return node;
                }
                AccessibilityNodeInfo scroller = scrollable(root);
                if (scroller != null) scroller.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            SystemClock.sleep(150);
        }
        throw new AssertionError("Missing visible control/text: " + text + "; visible tree: " + describe(root()));
    }
    private AccessibilityNodeInfo root() {
        AccessibilityNodeInfo node = test.getUiAutomation().getRootInActiveWindow();
        if (node != null) return node;
        for (android.view.accessibility.AccessibilityWindowInfo window : test.getUiAutomation().getWindows()) {
            if ((window.isActive() || window.isFocused()) && window.getRoot() != null) return window.getRoot();
        }
        return null;
    }
    private void stage(String value) {
        Bundle status = new Bundle(); status.putString("stream", "UI step: " + value + "\n"); test.sendStatus(0, status);
    }
    private String describe(AccessibilityNodeInfo node) {
        if (node == null) return "no active window";
        StringBuilder result = new StringBuilder();
        if (node.isVisibleToUser() && node.getText() != null) result.append('[').append(node.getText()).append(']');
        for (int i = 0; i < node.getChildCount(); i++) result.append(describe(node.getChild(i)));
        return result.toString();
    }
    private AccessibilityNodeInfo scrollable(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.isScrollable()) return node;
        for (int i = 0; i < node.getChildCount(); i++) { AccessibilityNodeInfo found = scrollable(node.getChild(i)); if (found != null) return found; }
        return null;
    }
    private void click(String text) {
        stage("click " + text);
        AccessibilityNodeInfo node = find(text);
        // Controls below the fold must be revealed as they would be for a user.
        node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.getId());
        boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        while (!clicked && node.getParent() != null) { node = node.getParent(); clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK); }
        if (!clicked) throw new AssertionError("Cannot click " + text);
        test.waitForIdleSync(); SystemClock.sleep(200);
    }
    private void set(String current, String value) {
        AccessibilityNodeInfo root = root();
        AccessibilityNodeInfo editor = editable(root);
        if (editor == null) throw new AssertionError("No accessible editor for " + current);
        Bundle args = new Bundle(); args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value);
        if (!editor.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) throw new AssertionError("Cannot edit " + current);
    }
    private AccessibilityNodeInfo editable(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.isEditable()) return node;
        for (int i = 0; i < node.getChildCount(); i++) { AccessibilityNodeInfo result = editable(node.getChild(i)); if (result != null) return result; }
        return null;
    }
    private void screenshot(Activity activity, String name) throws Exception {
        // Test-only capture of fictional data. Production keeps FLAG_SECURE enabled.
        test.runOnMainSync(() -> activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE));
        test.waitForIdleSync(); SystemClock.sleep(300);
        Bitmap image = test.getUiAutomation().takeScreenshot();
        if (image == null) {
            // Older SurfaceFlinger versions may return null after recreation.
            // Render the actual laid-out native view, preserving all UI assertions.
            final Bitmap[] rendered = new Bitmap[1];
            test.runOnMainSync(() -> {
                android.view.View root = activity.getWindow().getDecorView();
                rendered[0] = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
                root.draw(new android.graphics.Canvas(rendered[0]));
            });
            image = rendered[0];
        }
        File directory = test.getTargetContext().getExternalFilesDir(null);
        if (directory == null) throw new AssertionError("No test screenshot storage");
        try (FileOutputStream out = new FileOutputStream(new File(directory, name + ".png"))) {
            if (!image.compress(Bitmap.CompressFormat.PNG, 100, out)) throw new AssertionError("Cannot save screenshot");
        }
        image.recycle();
        test.runOnMainSync(() -> activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE));
    }
    void run() throws Exception {
        try { runChecks(); }
        catch (Exception | AssertionError failure) {
            try { if (active != null) screenshot(active, "failure"); } catch (Exception ignored) { }
            throw failure;
        }
    }
    private void runChecks() throws Exception {
        android.accessibilityservice.AccessibilityServiceInfo info = test.getUiAutomation().getServiceInfo();
        info.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        test.getUiAutomation().setServiceInfo(info);
        Activity activity = test.startActivitySync(new Intent().setClassName(test.getTargetContext(), "com.credittitan.ai.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        active = activity;
        test.waitForIdleSync(); SystemClock.sleep(500);
        find("Your credit. Your next move.");
        for (String label : new String[]{"Home", "Reports", "Disputes", "Coach", "More"}) {
            android.graphics.Rect bounds = new android.graphics.Rect(); find(label).getBoundsInScreen(bounds);
            if (bounds.isEmpty() || bounds.left < 0 || bounds.right > test.getTargetContext().getResources().getDisplayMetrics().widthPixels) throw new AssertionError("Navigation is clipped: " + label);
        }
        screenshot(activity, "dashboard");
        click("Reports"); find("Keep your reports together");
        click("Edit report notes"); set("report notes", "My statement shows a different balance."); click("Save locally");
        find("My statement shows a different balance.");
        screenshot(activity, "reports");
        click("Disputes"); click("Choose bureau"); click("Equifax");
        click("Create draft from report notes"); click("Replace draft");
        String draft = test.getTargetContext().getSharedPreferences("titan_local", 0).getString("dispute_draft", "");
        if (!draft.contains("To: Equifax") || !draft.contains("My statement shows")) throw new AssertionError("Draft generation failed");
        click("Edit dispute draft"); set("draft", "Unsaved draft survives recreation.");
        Instrumentation.ActivityMonitor monitor = test.addMonitor("com.credittitan.ai.MainActivity", null, false);
        Activity previous = activity;
        test.runOnMainSync(previous::recreate);
        Activity recreated = monitor.waitForActivityWithTimeout(5000);
        if (recreated == null) throw new AssertionError("Activity did not recreate");
        activity = recreated; active = activity; test.removeMonitor(monitor); test.waitForIdleSync(); SystemClock.sleep(300);
        stage("verify restored editor and save through its real dialog button");
        // API 24 accessibility can retain a stale active-window ID across recreation.
        // Inspect the actual restored dialog view; do not recreate or fill its state.
        java.lang.reflect.Field editorField = MainActivity.class.getDeclaredField("editor");
        editorField.setAccessible(true);
        Activity restoredActivity = activity;
        test.runOnMainSync(() -> {
            try {
                android.widget.EditText restored = (android.widget.EditText) editorField.get(restoredActivity);
                if (restored == null || !"Unsaved draft survives recreation.".contentEquals(restored.getText())) throw new AssertionError("Unsaved editor state was lost");
                android.view.View save = restored.getRootView().findViewById(android.R.id.button1);
                if (save == null || !save.performClick()) throw new AssertionError("Restored dialog has no working Save button");
            } catch (IllegalAccessException failure) { throw new AssertionError(failure); }
        });
        test.waitForIdleSync();
        if (!"Unsaved draft survives recreation.".equals(test.getTargetContext().getSharedPreferences("titan_local", 0).getString("dispute_draft", ""))) throw new AssertionError("Restored draft did not save");
        find("Unsaved draft survives recreation.");
        click("Review and export draft"); find("Export your draft?"); click("Cancel");
        click("Coach"); click("Understand different scores"); find("Understand different scores"); click("Close");
        click("More"); click("FICO Booster Plus"); screenshot(activity, "action-plan-top"); click("Read step 1"); click("Mark not done");
        if (test.getTargetContext().getSharedPreferences("titan_local", 0).getBoolean("boost0", true)) throw new AssertionError("Plan change not saved");
        click("Read step 1"); click("Mark completed");
        active = activity;
        test.runOnMainSync(() -> {
            Activity current = monitorActivity();
            ((android.widget.EditText) current.findViewById(R.id.utilization_balance)).setText("320");
            ((android.widget.EditText) current.findViewById(R.id.utilization_limit)).setText("1000");
        });
        click("Calculate utilization"); find("Reported utilization: 32.0%\nThis ratio is not a score prediction.");
        screenshot(activity, "action-plan");
        for (String name : new String[]{"Bureaus", "Progress", "Card Marketplace", "Credit Guardian", "Privacy & Data"}) {
            click("More"); click(name); find(name);
        }
        click("Clear this app's local data"); find("Delete your local workspace?"); click("Cancel");
        if (test.getTargetContext().getSharedPreferences("titan_local", 0).getString("report_notes", "").isEmpty()) throw new AssertionError("Cancel erased saved notes");
        click("Home"); find("Your credit. Your next move.");
        test.runOnMainSync(activity::finish);
    }
    private Activity active;
    private Activity monitorActivity() { return active; }
}
