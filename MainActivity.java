package com.credittitan.ai;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
 final int NAVY=Color.rgb(6,17,39), PANEL=Color.rgb(15,37,69), BLUE=Color.rgb(20,173,255), SILVER=Color.rgb(215,232,245);
 final String[] tabs={"Dashboard","Reports","Disputes","Cards","Coach","Progress"};
 LinearLayout content;
 int dp(int v){return (int)(getResources().getDisplayMetrics().density*v+0.5f);}
 GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),BLUE);return d;}
 TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(14),dp(12),dp(14),dp(12));return t;}
 void card(String heading,String detail){LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setBackground(bg(PANEL,16));TextView title=text(heading,18,SILVER);title.setTypeface(null,Typeface.BOLD);box.addView(title);box.addView(text(detail,14,Color.WHITE));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(12),dp(7),dp(12),dp(7));content.addView(box,p);}
 void action(String name,String message){Button b=new Button(this);b.setText(name);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackground(bg(PANEL,12));b.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(name).setMessage(message).setPositiveButton("OK",null).show());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(12),dp(6),dp(12),dp(6));content.addView(b,p);}
 @Override public void onCreate(Bundle state){super.onCreate(state);show("Dashboard");}
 void show(String tab){
 LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(NAVY);
 TextView brand=text("⬡  CREDIT TITAN AI",24,SILVER);brand.setGravity(Gravity.CENTER);brand.setTypeface(null,Typeface.BOLD);root.addView(brand);
 TextView subtitle=text("CREDIT • BUILDER • GUARDIAN",11,BLUE);subtitle.setGravity(Gravity.CENTER);root.addView(subtitle);
 ScrollView scroll=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(1);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
 card(tab,"Preview mode • All scores and offers below are fictional demonstration information.");
 switch(tab){
 case "Dashboard":card("Credit Score • 712","Sample FICO-style score display — not an actual FICO score.\nScore change: +18 points (sample)");card("Three Bureau Overview","Experian: 718   Equifax: 724   TransUnion: 719\nDemonstration values • No bureau connected");card("Credit Utilization","24% sample utilization • Lower balances can reduce utilization.");break;
 case "Reports":card("Credit Report Analyzer","Compare three-bureau reports, flag possible inconsistencies, and review account history.");action("Import Report","Secure credit report import is not connected yet. No report has been accessed.");break;
 case "Disputes":card("Dispute Center","Organize potentially inaccurate report items and prepare review letters.");action("Start Dispute","This is a preview. No dispute has been submitted or sent to a bureau.");break;
 case "Cards":card("Credit Card Marketplace","Explore fictional offers by credit profile. Sponsored placement and commissions must be disclosed.");card("Demo Offer • Starter Card","Annual fee: $0 • Example only • Not a real application.");card("Demo Offer • Rewards Card","Cash back: 1.5% • Example only • No bank connection.");break;
 case "Coach":card("AI Credit Coach","Educational preview: check your credit reports, make payments on time, and review utilization.");action("Ask AI Coach","Live AI service is not configured. Personalized financial recommendations are not yet available.");break;
 default:card("Score Progress","Sample score history: 694 → 703 → 712\nIllustrative change: +18 points.");card("Milestones","Review reports • Track balances • Resolve confirmed inaccuracies");}
 TextView credit=text("Created, Developed & Designed by Lanty Kennedy",12,SILVER);credit.setGravity(Gravity.CENTER);content.addView(credit);
 HorizontalScrollView h=new HorizontalScrollView(this);h.setHorizontalScrollBarEnabled(false);LinearLayout nav=new LinearLayout(this);nav.setPadding(dp(4),dp(5),dp(4),dp(5));
 for(String name:tabs){Button b=new Button(this);b.setText(name);b.setAllCaps(false);b.setTextSize(11);b.setTextColor(name.equals(tab)?NAVY:SILVER);b.setBackground(bg(name.equals(tab)?BLUE:PANEL,14));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(100),dp(52));p.setMargins(dp(3),0,dp(3),0);nav.addView(b,p);b.setOnClickListener(v->show(name));}
 h.addView(nav);root.addView(h);setContentView(root);
 }
}
