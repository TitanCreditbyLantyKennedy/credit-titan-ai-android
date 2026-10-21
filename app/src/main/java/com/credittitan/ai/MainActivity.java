package com.credittitan.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    private final int navy=Color.rgb(9,20,42), panel=Color.rgb(22,39,70), cyan=Color.rgb(83,211,240), silver=Color.rgb(200,215,230);
    private final String[] pages={"Dashboard","Reports","Disputes","AI Coach","FICO Booster Plus","Bureaus","Progress"};
    private LinearLayout content;
    private android.content.SharedPreferences prefs;
    @Override public void onCreate(Bundle state){super.onCreate(state);prefs=getSharedPreferences("titan_local",MODE_PRIVATE);show("Dashboard");}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(12),dp(10),dp(12),dp(10));return t;}
    private void card(String heading,String body){
        LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(10),dp(8),dp(10),dp(10));box.setBackground(shape(panel,18));
        TextView h=text(heading,19,silver);h.setTypeface(null,Typeface.BOLD);box.addView(h);
        box.addView(text(body,15,Color.WHITE));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(14));content.addView(box,p);
    }
    private void action(String label, Runnable task){
        TextView button=text(label,15,navy);button.setGravity(Gravity.CENTER);
        button.setTypeface(null,Typeface.BOLD);button.setBackground(shape(cyan,14));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.setMargins(0,0,0,dp(14));
        content.addView(button,lp);button.setOnClickListener(v->task.run());
    }
    private void note(String title,String message){
        new android.app.AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("OK",null).show();
    }
    private void input(String title,String key,String hint){
        EditText field=new EditText(this);field.setSingleLine(false);field.setMinLines(3);
        field.setHint(hint);field.setText(prefs.getString(key,""));
        new android.app.AlertDialog.Builder(this).setTitle(title).setView(field)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Save locally",(d,w)->{
                prefs.edit().putString(key,field.getText().toString()).apply();show(title.equals("Credit report notes")?"Reports":"Disputes");
            }).show();
    }
    private void show(String page){
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(navy);
        TextView brand=text("◆  CREDIT TITAN AI",24,silver);brand.setGravity(Gravity.CENTER);brand.setTypeface(null,Typeface.BOLD);root.addView(brand);
        TextView tagline=text("Credit Repair • Builder • Guardian",13,cyan);tagline.setGravity(Gravity.CENTER);root.addView(tagline);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(18),dp(18),dp(18),dp(8));scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        card(page,"DEMO MODE — Illustrative data, not a live credit report.");
        switch(page){
            case "Dashboard":
                card("Sample credit score: 682","This score is fictional and does not update from FICO or any credit bureau.");
                card("Three-bureau overview","Experian • Equifax • TransUnion\nConnection status: Not connected");
                card("Credit utilization","Example: 32% • Lower balances may reduce utilization.");break;
            case "Reports":card("Report analyzer","No bureau data connected. Record observations below; notes are saved only on this device.");
                card("Your report notes",prefs.getString("report_notes","No notes saved yet."));
                action("Add report notes",()->input("Credit report notes","report_notes","Record possible errors, dates, and account details. Avoid sensitive identifiers."));
                break;
            case "Disputes":card("Dispute center","Draft → Submitted → Response received\nNo disputes have been filed through this app.");
                card("Your draft",prefs.getString("dispute_draft","No draft saved yet."));
                action("Write a dispute draft",()->input("Dispute draft","dispute_draft","Describe the specific inaccurate item and why it is incorrect."));
                action("Important dispute information",()->note("Disputes","This app saves a draft only. It does not send disputes to a credit bureau or promise removal of accurate information."));
                break;
            case "FICO Booster Plus":
                card("60-Day Credit Action Plan","Educational checklist only. No live FICO score, score forecast, bureau connection, or automatic account changes.");
                String[] boost={ "Review revolving balances and reporting dates", "Check for late payments and set reminders", "Review credit reports for supportable inaccuracies", "Evaluate credit-limit increase inquiry policies", "Review effects before opening or closing accounts", "Recheck reports and scores using the same scoring model" };
                for(int i=0;i<boost.length;i++){final int n=i;boolean done=prefs.getBoolean("boost"+i,false);
                    action((done?"✓  ":"○  ")+boost[i],()->{prefs.edit().putBoolean("boost"+n,!prefs.getBoolean("boost"+n,false)).apply();show("FICO Booster Plus");});
                }
                action("Important score information",()->note("FICO Booster Plus","Actions may help credit health, but no FICO score increase or 60-day result is guaranteed. This demo does not access real credit reports."));
                break;
            case "AI Coach":card("Credit education","Review your reports for errors, make payments on time, and manage revolving balances. Live AI is not connected.");break;
            case "Bureaus":card("Credit bureaus","Experian: Not connected\nEquifax: Not connected\nTransUnion: Not connected");break;
            case "Progress":card("Illustrative goals","No real score history is available. Your checklist is stored locally.");
                String[] goals={"Review all three reports","Check payment due dates","Review card balances","Investigate report errors"};
                for(int i=0;i<goals.length;i++){final int n=i;boolean done=prefs.getBoolean("goal"+i,false);
                    action((done?"✓  ":"○  ")+goals[i],()->{prefs.edit().putBoolean("goal"+n,!prefs.getBoolean("goal"+n,false)).apply();show("Progress");});
                }break;
        }
        HorizontalScrollView navScroll=new HorizontalScrollView(this);navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav=new LinearLayout(this);nav.setPadding(dp(8),dp(6),dp(8),dp(6));
        for(String item:pages){
            TextView tab=text(item,13,item.equals(page)?navy:Color.WHITE);tab.setGravity(Gravity.CENTER);
            tab.setBackground(shape(item.equals(page)?cyan:panel,14));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(46));p.setMargins(dp(3),0,dp(3),0);nav.addView(tab,p);
            tab.setOnClickListener(v->show(item));
        }
        navScroll.addView(nav);root.addView(navScroll);
        TextView credit=text("Lanty Kennedy • Creator, Developer & Designer",11,cyan);credit.setGravity(Gravity.CENTER);root.addView(credit);
        setContentView(root);
    }
}