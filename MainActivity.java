package com.credittitan.ai;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.*;
public class MainActivity extends Activity {
 LinearLayout content; String[] tabs={"Dashboard","Reports","Disputes","Coach","Progress"};
 public void onCreate(Bundle b){super.onCreate(b);show("Dashboard");}
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(20,20,20,20);return t;}
 void card(String s){TextView t=label(s,18);t.setBackgroundColor(Color.rgb(22,46,82));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(10,10,10,10);content.addView(t,p);}
 void show(String tab){LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(9,20,42));root.addView(label("CREDIT TITAN AI",26));root.addView(label("Phase 2 • Fictional demo data",13));ScrollView sc=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(1);sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));LinearLayout nav=new LinearLayout(this);for(String n:tabs){Button btn=new Button(this);btn.setText(n.equals("Dashboard")?"Home":n);btn.setTextSize(10);btn.setAllCaps(false);btn.setOnClickListener(v->show(n));nav.addView(btn,new LinearLayout.LayoutParams(0,-2,1));}root.addView(nav);setContentView(root);card(tab);
 switch(tab){case "Dashboard":card("Sample credit score: 712 • Good");card("Three-bureau monitoring concept\nExperian • Equifax • TransUnion\nNot connected");card("Sample utilization: 24%");break;case "Reports":card("AI report analyzer preview\nNo real credit reports loaded");break;case "Disputes":card("Dispute assistance preview\nNo disputes are submitted");break;case "Coach":card("Educational tip: Review reports for inaccuracies and pay on time. Live AI not connected.");break;default:card("Sample score history: 694 → 703 → 712");}card("Created, Developed & Designed by Lanty Kennedy");}
}
