package com.credittitan.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    private final int navy=Color.rgb(9,20,42), panel=Color.rgb(22,39,70), cyan=Color.rgb(83,211,240), gold=Color.rgb(240,185,78);
    private final String[] pages={"Dashboard","Reports","Disputes","AI Coach","Bureaus","Progress"};
    private LinearLayout content;
    @Override public void onCreate(Bundle state){super.onCreate(state);show("Dashboard");}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(12),dp(10),dp(12),dp(10));return t;}
    private void card(String heading,String body){
        LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(10),dp(8),dp(10),dp(10));box.setBackground(shape(panel,18));
        TextView h=text(heading,19,gold);h.setTypeface(null,Typeface.BOLD);box.addView(h);
        box.addView(text(body,15,Color.WHITE));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(14));content.addView(box,p);
    }
    private void show(String page){
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(navy);
        TextView brand=text("◆  CREDIT TITAN AI",24,gold);brand.setGravity(Gravity.CENTER);brand.setTypeface(null,Typeface.BOLD);root.addView(brand);
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
            case "Reports":card("Report analyzer","No real credit reports have been uploaded. Future analysis must distinguish verified facts from suggestions.");break;
            case "Disputes":card("Dispute center","Draft → Submitted → Response received\nDemonstration workflow only; no disputes have been filed.");break;
            case "AI Coach":card("Credit education","Review your reports for errors, make payments on time, and manage revolving balances. Live AI is not connected.");break;
            case "Bureaus":card("Credit bureaus","Experian: Not connected\nEquifax: Not connected\nTransUnion: Not connected");break;
            case "Progress":card("Illustrative goals","Review all three credit reports. Track balances and payment dates. No real score history is available.");break;
        }
        HorizontalScrollView navScroll=new HorizontalScrollView(this);navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav=new LinearLayout(this);nav.setPadding(dp(8),dp(6),dp(8),dp(6));
        for(String item:pages){
            TextView tab=text(item,13,item.equals(page)?navy:Color.WHITE);tab.setGravity(Gravity.CENTER);
            tab.setBackground(shape(item.equals(page)?gold:panel,14));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(46));p.setMargins(dp(3),0,dp(3),0);nav.addView(tab,p);
            tab.setOnClickListener(v->show(item));
        }
        navScroll.addView(nav);root.addView(navScroll);
        TextView credit=text("Lanty Kennedy • Creator, Developer & Designer",11,cyan);credit.setGravity(Gravity.CENTER);root.addView(credit);
        setContentView(root);
    }
}