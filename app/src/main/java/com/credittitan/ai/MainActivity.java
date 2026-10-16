
package com.credittitan.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private LinearLayout root, content, nav;
    private final int navy=Color.rgb(8,20,44), cyan=Color.rgb(20,221,230), white=Color.WHITE;
    private final String[] pages={"Dashboard","Reports","Disputes","AI Coach","Bureaus","Progress"};
    @Override public void onCreate(Bundle state){super.onCreate(state); show("Dashboard");}
    private TextView label(String text,int size,int color){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(color);v.setPadding(12,14,12,14);return v;}
    private void show(String page){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(navy);
        TextView header=label("CREDIT TITAN AI",26,white);header.setGravity(Gravity.CENTER);root.addView(header);
        TextView sub=label("Your AI Credit Building Assistant",15,cyan);sub.setGravity(Gravity.CENTER);root.addView(sub);
        ScrollView scroll=new ScrollView(this);content=new LinearLayout(this);content.setPadding(18,12,18,12);content.setOrientation(LinearLayout.VERTICAL);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        section(page);
        HorizontalScrollView horizontal=new HorizontalScrollView(this);nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
        for(String p:pages){Button b=new Button(this);b.setText(p);b.setAllCaps(false);b.setOnClickListener(v->show(p));nav.addView(b);}horizontal.addView(nav);root.addView(horizontal);
        TextView credit=label("Lanty Kennedy • Creator, Developer & Designer",11,cyan);credit.setGravity(Gravity.CENTER);root.addView(credit);setContentView(root);
    }
    private void item(String title,String body){TextView t=label(title,21,cyan);content.addView(t);TextView b=label(body,16,white);content.addView(b);}
    private void section(String page){
        item(page,"DEMO MODE • Fictional information only");
        switch(page){
            case "Dashboard":item("Sample credit score: 682","Illustrative score only • No credit bureau connected");item("Your next steps","Review report details, check utilization, and set a payment reminder.");break;
            case "Reports":item("Sample report analysis","One illustrative late payment and 32% revolving utilization.");item("Review carefully","Confirm dates, balances, and account ownership before disputing any inaccurate item.");break;
            case "Disputes":item("Dispute tracker","No real disputes submitted. Example status: Draft → Submitted → Response received.");break;
            case "AI Coach":item("Credit-building guidance","Pay on time, check reports for errors, and consider lowering revolving balances. Guidance is educational, not a live AI service.");break;
            case "Bureaus":item("Experian • Equifax • TransUnion","Not connected. Monitoring data shown here is a demonstration, not a live credit report.");break;
            case "Progress":item("Illustrative goal","Reduce revolving utilization below 30% and review all three reports for accuracy.");break;
        }
    }
}
