package com.credittitan.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    private final int navy=Color.rgb(8,20,44), cyan=Color.rgb(20,221,230), white=Color.WHITE;
    private final String[] pages={"Dashboard","Reports","Disputes","AI Coach","Bureaus","Progress"};
    private LinearLayout content;
    private int balance=960, limit=3000;
    private boolean disputeDraft=false;
    private int dp(int value){return (int)(getResources().getDisplayMetrics().density*value+0.5f);}
    @Override public void onCreate(Bundle state){super.onCreate(state);show(0);}
    private TextView text(String value,int size,int color){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);
        t.setPadding(dp(12),dp(12),dp(12),dp(12));return t;
    }
    private void add(String title,String body){
        content.addView(text(title,20,cyan));
        content.addView(text(body,16,white));
    }
    private void action(String label,Runnable callback){
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setOnClickListener(v->callback.run());
        content.addView(b,new LinearLayout.LayoutParams(-1,-2));
    }
    private void show(int index){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(navy);
        TextView title=text("CREDIT TITAN AI",26,white);title.setGravity(Gravity.CENTER);root.addView(title);
        TextView subtitle=text("Credit Repair • Builder • Guardian",14,cyan);subtitle.setGravity(Gravity.CENTER);root.addView(subtitle);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(10),dp(8),dp(10),dp(8));
        scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        add(pages[index],"DEMO MODE — Fictional information only. No credit bureau or bank connected.");
        render(index);
        HorizontalScrollView bar=new HorizontalScrollView(this);bar.setHorizontalScrollBarEnabled(false);
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
        for(int i=0;i<pages.length;i++){
            final int selected=i;Button button=new Button(this);button.setText(pages[i]);button.setAllCaps(false);
            button.setOnClickListener(v->show(selected));nav.addView(button);
        }
        bar.addView(nav);root.addView(bar);
        TextView credit=text("Lanty Kennedy • Creator, Developer & Designer",11,cyan);credit.setGravity(Gravity.CENTER);
        root.addView(credit);setContentView(root);
    }
    private void render(int index){
        switch(index){
            case 0:
                add("Illustrative credit score: 682","This is not a FICO score or a live credit report.");
                add("Your next steps","Explore sample reports, practice utilization planning, and draft a demonstration dispute.");
                action("Open report analyzer",()->show(1));
                action("Plan credit utilization",()->show(5));
                break;
            case 1:
                add("Sample report analyzer","Fictional example: one late payment and 32% revolving utilization.");
                add("What to verify","Check account ownership, payment history, dates, balances, and supporting documents before disputing inaccuracies.");
                action("Create a sample dispute draft",()->{disputeDraft=true;show(2);});
                break;
            case 2:
                add("Dispute center",disputeDraft?"Sample draft created locally for this session. Nothing has been sent.":"No sample dispute draft created yet.");
                action("Create demonstration draft",()->{disputeDraft=true;show(2);});
                if(disputeDraft)action("Clear demonstration draft",()->{disputeDraft=false;show(2);});
                add("Important","Only dispute information you reasonably believe is inaccurate. This prototype does not submit disputes.");
                break;
            case 3:
                add("AI coach — educational demo","Pay bills on time, review reports for errors, and consider reducing revolving balances.");
                add("How utilization works","Utilization is your revolving balance divided by your credit limit. The planner lets you explore fictional scenarios.");
                action("Open utilization planner",()->show(5));
                add("Service status","This coach is not connected to a live AI service.");
                break;
            case 4:
                add("Experian","Not connected — demonstration only.");
                add("Equifax","Not connected — demonstration only.");
                add("TransUnion","Not connected — demonstration only.");
                add("Privacy","No personal credit information is collected by this demo.");
                break;
            case 5:
                int pct=(int)Math.round(100.0*balance/limit);
                add("Interactive utilization planner","Sample balance: $"+balance+" | Sample limit: $"+limit+" | Utilization: "+pct+"%");
                add("Adjust fictional balance","Move the slider to explore how utilization changes. This does not predict a credit score.");
                SeekBar slider=new SeekBar(this);slider.setMax(limit);slider.setProgress(balance);
                slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                    public void onProgressChanged(SeekBar s,int p,boolean fromUser){balance=p;}
                    public void onStartTrackingTouch(SeekBar s){}
                    public void onStopTrackingTouch(SeekBar s){show(5);}
                });
                content.addView(slider);
                add("Illustrative goal","At a $"+limit+" limit, a balance below $900 would be under 30% utilization. Actual scoring depends on many factors.");
                action("Reset example",()->{balance=960;show(5);});
                break;
        }
    }
}
