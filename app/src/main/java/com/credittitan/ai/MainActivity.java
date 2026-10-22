package com.credittitan.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int NAVY = Color.rgb(7, 17, 35), PANEL = Color.rgb(18, 35, 58),
            CYAN = Color.rgb(135, 226, 250), SILVER = Color.rgb(219, 230, 242), MUTED = Color.rgb(164, 184, 205);
    private static final int IMPORT_REPORT = 41, EXPORT_DRAFT = 42;
    private static final String[] PAGES = {"Dashboard", "Reports", "Disputes", "AI Coach", "More", "FICO Booster Plus", "Bureaus", "Progress", "Card Marketplace", "Credit Guardian", "Privacy & Data"};
    private static final String[] STEPS = {"Review balances and reporting dates", "Set payment reminders", "Review reports for inaccuracies", "Check credit-limit inquiry policies", "Review account changes", "Recheck reports consistently"};
    private static final String[] GUIDANCE = {
            "Compare each card's reported balance, limit, and statement closing date. Use the utilization calculator to understand the ratio. Score effects vary by report and scoring model.",
            "Review due dates and consider reminders or automatic minimum payments with your lender. Check available funds and payment confirmations. Contact the lender promptly if you may miss a payment.",
            "Compare Experian, Equifax, and TransUnion reports. Record information you reasonably believe is inaccurate and collect supporting records. Accurate negative information is not automatically removable.",
            "Ask the issuer whether a credit-limit increase request requires a hard inquiry. Consider your circumstances and avoid increasing spending merely because a limit increases.",
            "Consider account age, balances, fees, and possible inquiries before opening or closing accounts. This tool does not recommend a specific account change or predict a score increase.",
            "Compare scores from the same model and version after report updates. Different models can give different results. Checklist completion does not represent a score change."
    };
    private LinearLayout content;
    private ScrollView scroll;
    private SharedPreferences prefs;
    private LocalReports reports;
    private String page = "Dashboard", exportText = "";
    private static final java.util.concurrent.atomic.AtomicBoolean importing = new java.util.concurrent.atomic.AtomicBoolean();
    private static java.lang.ref.WeakReference<MainActivity> visible = new java.lang.ref.WeakReference<>(null);
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private EditText editor;
    private String editorKey;
    private String restoredEditorKey, restoredEditorText;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("titan_local", MODE_PRIVATE);
        reports = new LocalReports(getFilesDir());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        if (state != null) {
            page = state.getString("page", "Dashboard");
            exportText = state.getString("export", "");
            restoredEditorKey = state.getString("editorKey");
            restoredEditorText = state.getString("editorText");
        }
        show(page);
        if (state != null) {
            final int position = state.getInt("scroll");
            scroll.post(() -> scroll.scrollTo(0, position));
        }
        if (restoredEditorKey != null) edit(restoredEditorKey, restoredEditorText);
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("page", page);
        state.putString("export", exportText);
        state.putInt("scroll", scroll.getScrollY());
        if (editor != null) {
            state.putString("editorKey", editorKey);
            state.putString("editorText", editor.getText().toString());
        }
    }
    @Override protected void onResume() {
        super.onResume(); visible = new java.lang.ref.WeakReference<>(this);
        if (page.equals("Reports") && editor == null && !importing.get()) show("Reports");
    }
    @Override protected void onPause() {
        if (visible.get() == this) visible.clear();
        super.onPause();
    }
    @Override protected void onDestroy() { io.shutdown(); super.onDestroy(); }
    @Override public void onBackPressed() { if (!page.equals("Dashboard")) show("Dashboard"); else super.onBackPressed(); }
    private int dp(float n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(0, dp(5), 0, dp(5)); t.setLineSpacing(dp(3), 1); return t;
    }
    private LinearLayout column() { LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); return box; }
    private LinearLayout card(String title, String body) {
        LinearLayout box = column(); box.setPadding(dp(18), dp(14), dp(18), dp(16));
        GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{PANEL, Color.rgb(12, 26, 46)});
        background.setCornerRadius(dp(20)); background.setStroke(dp(1), Color.rgb(44, 69, 94)); box.setBackground(background);
        TextView heading = text(title, 19, SILVER); heading.setTypeface(null, Typeface.BOLD); box.addView(heading);
        if (!body.isEmpty()) box.addView(text(body, 15, MUTED));
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(-1, -2); layout.setMargins(0, 0, 0, dp(14)); content.addView(box, layout);
        return box;
    }
    private Button button(String label, Runnable task, boolean primary) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(14);
        b.setTypeface(null, Typeface.BOLD); b.setTextColor(primary ? NAVY : CYAN);
        b.setPadding(dp(12), dp(12), dp(12), dp(12)); b.setMinHeight(dp(52)); b.setMinimumHeight(dp(52));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x3353D3F0), shape(primary ? CYAN : PANEL, 14), null));
        b.setOnClickListener(v -> task.run()); return b;
    }
    private void action(String label, Runnable task) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.setMargins(0, 0, 0, dp(12));
        content.addView(button(label, task, true), lp);
    }
    private void link(LinearLayout box, String label, Runnable task) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.topMargin = dp(10);
        box.addView(button(label, task, false), lp);
    }
    private void note(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("Close", null).show();
    }
    private void show(String requested) {
        boolean found = false; for (String valid : PAGES) if (valid.equals(requested)) found = true;
        page = found ? requested : "Dashboard";
        LinearLayout root = column(); root.setBackgroundColor(NAVY);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        LinearLayout brand = new LinearLayout(this); brand.setGravity(Gravity.CENTER_VERTICAL); brand.setPadding(dp(20), dp(10), dp(20), dp(8));
        ImageView shield = new ImageView(this); shield.setImageResource(com.credittitan.ai.R.drawable.ic_titan_shield); shield.setContentDescription("Credit Titan AI shield");
        brand.addView(shield, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout wordmark = column(); wordmark.setPadding(dp(10), 0, 0, 0);
        TextView name = text("CREDIT TITAN AI", 19, SILVER); name.setTypeface(null, Typeface.BOLD); wordmark.addView(name);
        wordmark.addView(text("REPAIR  /  BUILD  /  GUARD", 10, CYAN)); brand.addView(wordmark); root.addView(brand);
        scroll = new ScrollView(this); scroll.setFillViewport(true);
        content = column(); content.setPadding(dp(20), dp(10), dp(20), dp(16)); scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView mode = text("OFFLINE DEMO  •  PRIVATE LOCAL TOOLS", 10, CYAN); content.addView(mode);
        TextView title = text(page.equals("Dashboard") ? "Your credit. Your next move." : page, 27, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD); content.addView(title);
        if (!page.equals("Dashboard")) content.addView(text("Illustrative data • No connected accounts", 12, MUTED));
        switch (page) {
            case "Dashboard": dashboard(); break;
            case "Reports": reportPage(); break;
            case "Disputes": disputePage(); break;
            case "AI Coach": coachPage(); break;
            case "FICO Booster Plus": boostPage(); break;
            case "Bureaus": bureauPage(); break;
            case "Progress": progressPage(); break;
            case "Card Marketplace": marketplace(); break;
            case "Credit Guardian": guardian(); break;
            case "Privacy & Data": privacy(); break;
            default: more();
        }
        LinearLayout nav = new LinearLayout(this); nav.setPadding(dp(8), dp(6), dp(8), dp(6)); nav.setBackgroundColor(Color.rgb(12, 25, 44));
        String[] labels = {"Home", "Reports", "Disputes", "Coach", "More"};
        String[] targets = {"Dashboard", "Reports", "Disputes", "AI Coach", "More"};
        String selected = page; if (!java.util.Arrays.asList(targets).contains(page)) selected = "More";
        for (int i = 0; i < labels.length; i++) {
            final String target = targets[i]; boolean active = target.equals(selected);
            Button tab = button(labels[i], () -> show(target), active); tab.setTextSize(10); tab.setSingleLine(true); tab.setPadding(dp(2), dp(10), dp(2), dp(10));
            tab.setSelected(active); tab.setContentDescription(labels[i] + (active ? ", selected" : ", navigation"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1); lp.setMargins(dp(2), 0, dp(2), 0); nav.addView(tab, lp);
        }
        root.addView(nav);
        TextView credit = text("Lanty Kennedy • Creator, Developer & Designer", 10, MUTED); credit.setGravity(Gravity.CENTER); root.addView(credit);
        setContentView(root); root.requestApplyInsets();
    }
    private int completed(String prefix, int count) {
        int result = 0; for (int i = 0; i < count; i++) if (prefs.getBoolean(prefix + i, false)) result++; return result;
    }
    private void progressBar(LinearLayout box, int done, int total) {
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(total); bar.setProgress(done); bar.setProgressTintList(ColorStateList.valueOf(CYAN));
        box.addView(bar, new LinearLayout.LayoutParams(-1, dp(12)));
        box.addView(text(done + " of " + total + " complete", 13, CYAN));
    }
    private void dashboard() {
        LinearLayout score = card("Your credit snapshot", "SAMPLE SCORE • Not a live FICO score");
        score.addView(new ScoreView(), new LinearLayout.LayoutParams(-1, dp(166)));
        score.addView(text("682 is fictional. Your checklist never changes this sample score.", 12, MUTED));
        LinearLayout plan = card("Build your next chapter", "A practical credit-health checklist, one step at a time.");
        progressBar(plan, completed("boost", STEPS.length), STEPS.length);
        link(plan, "Open FICO Booster Plus", () -> show("FICO Booster Plus"));
        LinearLayout bureau = card("Three-bureau overview", "Experian  /  Equifax  /  TransUnion\nAll three: Not connected");
        link(bureau, "Review bureau status", () -> show("Bureaus"));
        LinearLayout report = card("Your report workspace", reports.list().length + " private PDF copies • " + (prefs.getString("report_notes", "").isEmpty() ? "No notes yet" : "Notes saved"));
        link(report, "Open report workspace", () -> show("Reports"));
    }
    private void reportPage() {
        card("Keep your reports together", "Import a private PDF copy from your device or document provider. Reports stay in this app's private storage. Automated extraction, cross-bureau comparison, and AI analysis are not connected.");
        action("Import a report PDF", () -> new AlertDialog.Builder(this).setTitle("Save a private PDF copy?")
                .setMessage("Only import a report you are authorized to use. The app will copy it into private device storage and will not upload it. Up to nine PDFs, 20 MB each. A PDF header check does not verify its contents.")
                .setNegativeButton("Cancel", null).setPositiveButton("Choose PDF", (d, w) -> {
                    if (importing.get()) { note("Import in progress", "Please wait for the current import to finish."); return; }
                    try { startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/pdf").addCategory(Intent.CATEGORY_OPENABLE), IMPORT_REPORT); }
                    catch (android.content.ActivityNotFoundException unavailable) { note("File picker unavailable", "This device needs a document provider to choose a PDF."); }
                }).show());
        File[] copies = reports.list();
        if (copies.length == 0) card("No reports imported", "Start with a PDF from Experian, Equifax, or TransUnion. Importing a file does not connect a bureau.");
        for (int i = 0; i < copies.length; i++) {
            File file = copies[i]; String bureau = prefs.getString("bureau_" + file.getName(), "Unassigned bureau");
            LinearLayout box = card("Local report " + (i + 1), bureau + " • " + Math.max(1, file.length() / 1024) + " KB\nStored only • Not analyzed");
            link(box, "Assign bureau", () -> new AlertDialog.Builder(this).setTitle("Which bureau issued this report?")
                    .setItems(CreditTools.BUREAUS, (d, n) -> { prefs.edit().putString("bureau_" + file.getName(), CreditTools.BUREAUS[n]).apply(); show("Reports"); }).show());
            link(box, "Remove local copy", () -> new AlertDialog.Builder(this).setTitle("Remove this local copy?")
                    .setMessage("The original file you selected will remain with its document provider.")
                    .setNegativeButton("Cancel", null).setPositiveButton("Remove", (d, w) -> {
                        try { if (!reports.delete(file)) throw new IOException(); prefs.edit().remove("bureau_" + file.getName()).apply(); show("Reports"); }
                        catch (IOException failed) { note("Unable to remove copy", "Please try again."); }
                    }).show());
        }
        card("Your report notes", prefs.getString("report_notes", "No notes saved yet. Note possible balance, date, status, duplicate-account, or identity inaccuracies for your review."));
        action("Edit report notes", () -> edit("report_notes", null));
        action("Review notes in Dispute Center", () -> show("Disputes"));
    }
    private void edit(String key, String restored) {
        editorKey = key;
        editor = new EditText(this); editor.setTextColor(NAVY); editor.setHintTextColor(Color.DKGRAY); editor.setTextSize(16);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        String initial = restored == null ? prefs.getString(key, "") : restored;
        int limit = Math.max(key.equals("dispute_draft") ? 24000 : 12000, initial.length());
        editor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(limit)}); editor.setMinLines(5); editor.setGravity(Gravity.TOP);
        editor.setPadding(dp(18), dp(12), dp(18), dp(12));
        editor.setHint("Describe the specific information. Avoid full account numbers and Social Security numbers.");
        editor.setText(initial);
        boolean draft = key.equals("dispute_draft");
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(draft ? "Edit dispute draft" : "Edit report notes").setView(editor)
                .setNegativeButton("Cancel", null).setPositiveButton("Save locally", null).create();
        dialog.setOnDismissListener(d -> { editor = null; editorKey = null; });
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = editor.getText().toString();
            if (!prefs.edit().putString(key, value).commit()) { editor.setError("Unable to save. Please try again."); return; }
            dialog.dismiss(); show(draft ? "Disputes" : "Reports");
        }));
        dialog.show();
    }
    private void disputePage() {
        card("You stay in control", "Review your report notes, choose a bureau, and create an editable draft. Nothing is sent by this app. A possible inaccuracy is not an established legal violation.");
        String bureau = prefs.getString("draft_bureau", CreditTools.BUREAUS[0]);
        LinearLayout target = card("New draft recipient: " + bureau, "Choose the bureau whose report contains the disputed information. This choice applies to the next generated draft; it does not change an existing saved draft.");
        link(target, "Choose bureau", () -> new AlertDialog.Builder(this).setTitle("Draft recipient").setItems(CreditTools.BUREAUS, (d, n) -> {
            prefs.edit().putString("draft_bureau", CreditTools.BUREAUS[n]).apply(); show("Disputes");
        }).show());
        action("Create draft from report notes", () -> {
            String notes = prefs.getString("report_notes", "").trim();
            if (notes.isEmpty()) { note("Add report notes first", "In Reports, describe the specific information you believe is inaccurate and why. Then return here to create a draft."); return; }
            Runnable create = () -> { prefs.edit().putString("dispute_draft", CreditTools.draft(bureau, notes)).apply(); show("Disputes"); };
            if (!prefs.getString("dispute_draft", "").isEmpty()) new AlertDialog.Builder(this).setTitle("Replace the saved draft?")
                    .setMessage("There is one saved draft on this device. Export it first if you want to keep a copy.")
                    .setNegativeButton("Cancel", null).setPositiveButton("Replace draft", (d, w) -> create.run()).show();
            else create.run();
        });
        String draft = prefs.getString("dispute_draft", "");
        TextView body = text(draft.isEmpty() ? "No draft saved yet." : draft, 14, SILVER); body.setTextIsSelectable(true);
        card("Your saved draft", "Private to this device").addView(body);
        action("Edit dispute draft", () -> edit("dispute_draft", null));
        action("Review and export draft", () -> {
            if (draft.trim().isEmpty()) { note("No draft to export", "Write a draft or create one from your report notes first."); return; }
            new AlertDialog.Builder(this).setTitle("Export your draft?").setMessage("Check the recipient, facts, requested correction, and placeholders. Export creates a plain-text file in your chosen location; that file is outside the app's private storage. This does not mail or submit a dispute.")
                    .setNegativeButton("Cancel", null).setPositiveButton("Choose save location", (d, w) -> {
                        exportText = draft;
                        try { startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain").putExtra(Intent.EXTRA_TITLE, "CreditTitanAI-dispute-draft.txt"), EXPORT_DRAFT); }
                        catch (android.content.ActivityNotFoundException missing) { note("File picker unavailable", "This device needs a document provider to export a draft."); }
                    }).show();
        });
        card("Before you send", "Verify the bureau's current mailing address and requirements. Keep copies of your letter and supporting records. If you choose certified mail, keep the receipt and delivery record. Deadline and response tracking are not connected yet.");
    }
    private void boostPage() {
        LinearLayout plan = card("Your credit action plan", "Six educational steps. No score forecast or guaranteed timeframe.");
        progressBar(plan, completed("boost", STEPS.length), STEPS.length);
        for (int i = 0; i < STEPS.length; i++) {
            final int n = i; boolean done = prefs.getBoolean("boost" + n, false);
            LinearLayout step = card((done ? "✓ " : (i + 1) + ". ") + STEPS[n], done ? "Completed" : "Ready when you are");
            link(step, "Read step " + (n + 1), () -> new AlertDialog.Builder(this).setTitle(STEPS[n]).setMessage(GUIDANCE[n])
                    .setNegativeButton("Close", null).setPositiveButton(done ? "Mark not done" : "Mark completed", (d, w) -> {
                        prefs.edit().putBoolean("boost" + n, !done).apply(); show("FICO Booster Plus");
                    }).show());
        }
        utilization();
    }
    private EditText numberField(String hint) {
        EditText field = new EditText(this); field.setTextColor(SILVER); field.setHintTextColor(MUTED); field.setHint(hint);
        field.setContentDescription(hint); field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)}); return field;
    }
    private void utilization() {
        LinearLayout tool = card("Utilization calculator", "Use the reported balance and credit limit for one revolving account. Nothing is sent or changed.");
        EditText balance = numberField("Reported balance in dollars"); balance.setId(R.id.utilization_balance);
        EditText limit = numberField("Credit limit in dollars"); limit.setId(R.id.utilization_limit);
        tool.addView(balance); tool.addView(limit);
        TextView result = text("Enter a balance and limit to calculate.", 15, CYAN); result.setId(R.id.utilization_result); result.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        link(tool, "Calculate utilization", () -> {
            try { result.setText(getString(R.string.utilization_result, CreditTools.utilization(balance.getText().toString(), limit.getText().toString()))); }
            catch (IllegalArgumentException invalid) { result.setText(invalid.getMessage()); }
        });
        tool.addView(result);
    }
    private void coachPage() {
        card("Credit Coach learning room", "Choose a topic for prepared educational guidance. Live AI chat is not connected, and no report is being analyzed.");
        String[] topics = {"Understand utilization", "Review a possible report error", "Build a payment routine", "Understand different scores"};
        String[] replies = {GUIDANCE[0], GUIDANCE[2], GUIDANCE[1], GUIDANCE[5]};
        for (int i = 0; i < topics.length; i++) { final int n = i; action(topics[i], () -> note(topics[n], replies[n])); }
        utilization();
    }
    private void bureauPage() {
        for (String bureau : CreditTools.BUREAUS) {
            int count = 0; for (File f : reports.list()) if (bureau.equals(prefs.getString("bureau_" + f.getName(), ""))) count++;
            LinearLayout box = card(bureau, "Not connected • No live score\n" + count + " assigned private PDF copies. Imported files have not been analyzed.");
            link(box, "Open report workspace", () -> show("Reports"));
        }
    }
    private void progressPage() {
        LinearLayout box = card("The work you have done", "Your saved checklist reflects actions you marked complete. No real score history is available.");
        progressBar(box, completed("boost", STEPS.length), STEPS.length);
        String[] goals = {"Review all three reports", "Check payment due dates", "Review card balances", "Investigate report errors"};
        for (int i = 0; i < goals.length; i++) { final int n = i; boolean done = prefs.getBoolean("goal" + n, false);
            action((done ? "✓ " : "○ ") + goals[n], () -> { prefs.edit().putBoolean("goal" + n, !done).apply(); show("Progress"); });
        }
        action("Continue action plan", () -> show("FICO Booster Plus"));
    }
    private void marketplace() {
        card("Compare before you apply", "FICTIONAL DEMONSTRATION OFFERS\nNo bank partnerships, applications, personalized matching, or approvals are connected. These examples are not available products.");
        String[] names = {"Foundation Secured — Demo", "Everyday Builder — Demo", "Titan Rewards — Demo"};
        String[] details = {"Example annual fee: $0\nExample variable purchase APR: 27.99%\nExample refundable deposit: $200\nExample rewards: None",
                "Example annual fee: $39\nExample variable purchase APR: 29.99%\nExample deposit: None\nExample rewards: None",
                "Example annual fee: $0\nExample variable purchase APR: 24.99%\nExample deposit: None\nExample rewards: 1% cash back"};
        for (int i = 0; i < names.length; i++) { final int n = i;
            LinearLayout offer = card(names[n], details[n]);
            link(offer, "View demo offer details", () -> note(names[n], details[n] + "\n\nFictional example only. No issuer or eligibility assessment. Actual offers require current terms, fees, disclosures, and a lender's decision. No application is sent."));
        }
        card("Our marketplace direction", "The planned model charges participating banks through advertising and approved-account commissions. Paid placement and compensation will be clearly disclosed when real offers are available. Consumers will not be charged to compare these offers.");
    }
    private void guardian() {
        card("Credit Guardian", "Your personal review routine. Live alerts, identity monitoring, and automatic protection are not connected.");
        String[] items = {"Review unfamiliar accounts or inquiries", "Check personal details on each report", "Save supporting records securely"};
        for (int i = 0; i < items.length; i++) { final int n = i; boolean done = prefs.getBoolean("guard" + n, false);
            action((done ? "✓ " : "○ ") + items[n], () -> { prefs.edit().putBoolean("guard" + n, !done).apply(); show("Credit Guardian"); });
        }
    }
    private void privacy() {
        card("Your data on this device", "This build has no internet permission. Notes, drafts, checklists, and imported PDFs stay in app-private storage. System screenshots are blocked. Device security still matters; storage is not protected by a separate app lock. Android backup is disabled.");
        card("You control exports", "Exporting a draft saves a plain-text copy in a location you choose. That copy is outside the app's private storage and is not removed when you clear this app's local data.");
        card("Development status", "Offline foundation • Version 1.0.3\nReal AI analysis, bureau access, mailing services, alerts, and bank integrations need secure service connections before public launch.");
        action("Clear this app's local data", () -> new AlertDialog.Builder(this).setTitle("Delete your local workspace?")
                .setMessage("This removes imported PDF copies, notes, the saved draft, and all checklist progress from this app. Your original files and exported copies will remain. This cannot be undone.")
                .setNegativeButton("Cancel", null).setPositiveButton("Delete local data", (d, w) -> {
                    if (importing.get()) { note("Import in progress", "Wait for the import to finish before clearing data."); return; }
                    try { reports.clear(); }
                    catch (IOException failure) { note("Could not remove all reports", "Some local copies could not be removed. Please try again."); return; }
                    if (!prefs.edit().clear().commit()) { note("Could not clear saved data", "Please try again."); return; }
                    exportText = ""; show("Dashboard");
                }).show());
    }
    private void more() {
        card("Your credit toolkit", "Everything in one place. Keep building at your pace.");
        String[] names = {"FICO Booster Plus", "Bureaus", "Progress", "Card Marketplace", "Credit Guardian", "Privacy & Data"};
        for (String name : names) action(name, () -> show(name));
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK || data == null || data.getData() == null) { if (request == EXPORT_DRAFT) exportText = ""; return; }
        android.net.Uri uri = data.getData();
        if (request == IMPORT_REPORT) {
            if (!importing.compareAndSet(false, true)) return;
            Toast.makeText(this, "Saving private PDF copy…", Toast.LENGTH_SHORT).show();
            io.execute(() -> {
                String message;
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    if (input == null) throw new IOException("The document provider did not return a file.");
                    reports.save(input); message = "PDF copy saved. It has not been analyzed. You can assign its bureau in Reports.";
                } catch (IOException | SecurityException | IllegalArgumentException failure) { message = "The PDF could not be imported. Choose a PDF up to 20 MB and check available storage and file access."; }
                String status = message;
                runOnUiThread(() -> {
                    importing.set(false);
                    MainActivity active = visible.get();
                    if (active != null && !active.isFinishing() && !active.isDestroyed()) {
                        if (active.page.equals("Reports") && active.editor == null) active.show("Reports");
                        active.note("Report import", status);
                    }
                });
            });
        } else if (request == EXPORT_DRAFT) {
            final String snapshot = exportText; exportText = "";
            if (snapshot.isEmpty()) { note("No export pending", "Return to Disputes and review the draft again."); return; }
            io.execute(() -> {
                String message;
                try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                    if (out == null) throw new IOException();
                    out.write(snapshot.getBytes(StandardCharsets.UTF_8)); message = "Draft exported. It has not been mailed or submitted.";
                } catch (IOException | SecurityException | IllegalArgumentException failure) { message = "The draft could not be exported. Your saved draft remains in Disputes."; }
                String status = message;
                runOnUiThread(() -> {
                    MainActivity active = visible.get();
                    if (active != null && !active.isFinishing() && !active.isDestroyed()) active.note("Draft export", status);
                });
            });
        }
    }
    private final class ScoreView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF arc = new RectF();
        private LinearGradient gradient;
        @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            float center = w / 2f, radius = Math.min(w * .38f, dp(100)), y = dp(112);
            arc.set(center - radius, y - radius, center + radius, y + radius);
            gradient = new LinearGradient(center - radius, 0, center + radius, 0, new int[]{0xff386a98, CYAN, SILVER}, null, Shader.TileMode.CLAMP);
        }
        ScoreView() { super(MainActivity.this); setContentDescription("Fictional sample credit score 682, on a scale of 300 to 850. Not connected."); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float center = getWidth() / 2f, radius = Math.min(getWidth() * .38f, dp(100)), y = dp(112);
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeWidth(dp(10));
            paint.setColor(Color.rgb(39, 60, 82)); c.drawArc(arc, 180, 180, false, paint);
            paint.setShader(gradient);
            c.drawArc(arc, 180, 125, false, paint); paint.setShader(null);
            paint.setStyle(Paint.Style.FILL); paint.setColor(SILVER); paint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            paint.setTextSize(dp(54)); paint.setTextAlign(Paint.Align.CENTER); c.drawText("682", center, y - dp(4), paint);
            paint.setColor(MUTED); paint.setTextSize(dp(12)); c.drawText("300                         850", center, y + dp(24), paint);
        }
    }
}
