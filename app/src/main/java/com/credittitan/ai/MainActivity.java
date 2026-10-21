package com.credittitan.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private final int navy=Color.rgb(7,16,34), panel=Color.rgb(17,31,57), cyan=Color.rgb(66,216,239),
        white=Color.rgb(241,247,255), muted=Color.rgb(166,184,209), gold=Color.rgb(236,187,88), border=Color.rgb(43,66,96);
    private final String[] pages={"Dashboard","Reports","Disputes","AI Coach","Bureaus","Progress"};
    private LinearLayout content;
    private int balance=960, limit=3000;
    private boolean disputeDraft=false;
    private int dp(int value){return (int)(getResources().getDisplayMetrics().density*value+0.5f);}
    private int score(){int pct=(int)Math.round(100.0*balance/limit);return Math.max(600,Math.min(730,682+(32-pct)/2));}
    private String note(){return "Educational illustration only — NOT a FICO score, credit-bureau score, or prediction. Actual credit scoring considers many factors.";}
    private GradientDrawable shape(int fill,int stroke,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));
        if(stroke!=0)d.setStroke(dp(1),stroke);return d;
    }
    private TextView text(String value,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    private LinearLayout column(){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;
    }
    private void brand(LinearLayout root){
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(20),dp(14),dp(20),dp(12));
        TextView shield=text("T",23,gold,true);shield.setGravity(Gravity.CENTER);
        shield.setBackground(shape(panel,gold,14));
        head.addView(shield,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout name=column();name.setPadding(dp(12),0,0,0);
        name.addView(text("CREDIT  TITAN  AI",20,white,true));
        TextView tagline=text("CREDIT REPAIR  •  BUILDER  •  GUARDIAN",9,cyan,true);
        tagline.setLetterSpacing(0.06f);tagline.setPadding(0,dp(5),0,0);name.addView(tagline);
        head.addView(name);root.addView(head);
        View line=new View(this);line.setBackgroundColor(border);
        root.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
    }
    private void card(String heading,String body){
        LinearLayout panelView=column();panelView.setPadding(dp(17),dp(15),dp(17),dp(16));
        panelView.setBackground(shape(panel,border,18));
        TextView h=text(heading,18,white,true);panelView.addView(h);
        TextView b=text(body,14,muted,false);b.setLineSpacing(dp(3),1f);b.setPadding(0,dp(8),0,0);panelView.addView(b);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);
        content.addView(panelView,lp);
    }
    private void action(String label,Runnable callback){
        TextView b=text(label+"   ›",15,navy,true);b.setGravity(Gravity.CENTER);
        b.setBackground(shape(cyan,0,12));b.setPadding(dp(12),dp(15),dp(12),dp(15));
        b.setOnClickListener(v->callback.run());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);content.addView(b,lp);
    }
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(navy);getWindow().setNavigationBarColor(navy);show(0);}
    private void show(int index){
        LinearLayout root=column();root.setBackgroundColor(navy);root.setFitsSystemWindows(true);
        brand(root);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);
        content=column();content.setPadding(dp(18),dp(24),dp(18),dp(20));scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView title=text(pages[index],26,white,true);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);titleLp.bottomMargin=dp(6);content.addView(title,titleLp);
        TextView caption=text("DEMONSTRATION  /  FICTIONAL DATA",11,gold,true);
        LinearLayout.LayoutParams captionLp=new LinearLayout.LayoutParams(-1,-2);captionLp.bottomMargin=dp(20);content.addView(caption,captionLp);
        render(index);
        View divider=new View(this);divider.setBackgroundColor(border);root.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
        HorizontalScrollView scroller=new HorizontalScrollView(this);scroller.setHorizontalScrollBarEnabled(false);
        scroller.setPadding(dp(10),dp(9),dp(10),dp(9));scroller.setClipToPadding(false);
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
        for(int i=0;i<pages.length;i++){
            final int target=i;boolean selected=(index==i);
            TextView tab=text(pages[i],13,selected?navy:muted,selected);
            tab.setGravity(Gravity.CENTER);tab.setPadding(dp(16),dp(12),dp(16),dp(12));
            tab.setBackground(shape(selected?cyan:panel,selected?0:border,12));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.rightMargin=dp(7);
            nav.addView(tab,lp);tab.setOnClickListener(v->show(target));
        }
        scroller.addView(nav);root.addView(scroller);
        TextView footer=text("LANTY KENNEDY  •  CREATOR, DEVELOPER & DESIGNER",10,muted,true);
        footer.setGravity(Gravity.CENTER);footer.setPadding(dp(8),dp(7),dp(8),dp(13));root.addView(footer);
        setContentView(root);
    }
    private void render(int index){
        switch(index){
            case 0:
                card("ILLUSTRATIVE SCORE INDEX  •  "+score(),note());
                card("Your financial command center","Explore sample reports, model utilization scenarios, and practice preparing a dispute. No bank or bureau is connected.");
                action("Analyze sample credit report",()->show(1));
                action("Open utilization simulator",()->show(5));
                break;
            case 1:
                card("Sample report overview","Fictional report example: one late payment and 32% revolving utilization.");
                card("Review before disputing","Verify account ownership, payment dates, reported balances, and supporting documents. Dispute only information you believe is inaccurate.");
                action("Create a sample dispute draft",()->{disputeDraft=true;show(2);});
                break;
            case 2:
                card("Dispute workspace",disputeDraft?"A demonstration dispute draft is ready for review. Nothing has been submitted.":"No dispute draft yet. Start a sample to see the workflow.");
                action("Create demonstration draft",()->{disputeDraft=true;show(2);});
                if(disputeDraft)action("Clear sample draft",()->{disputeDraft=false;show(2);});
                card("Important","This prototype does not submit disputes or access credit files.");
                break;
            case 3:
                card("Your AI Coach — demo","Educational guidance: pay on time, check reports for inaccuracies, and manage revolving balances.");
                card("Smart utilization planning","Explore hypothetical balances and limits. This is not a live AI service.");
                action("Explore credit utilization",()->show(5));
                break;
            case 4:
                card("Experian","Not connected — demonstration only.");
                card("Equifax","Not connected — demonstration only.");
                card("TransUnion","Not connected — demonstration only.");
                card("Your privacy","This demonstration does not collect or retrieve personal credit reports.");
                break;
            case 5:
                int pct=(int)Math.round(100.0*balance/limit);
                card("Utilization simulator","Fictional credit limit: $"+limit+". Adjust the sample balance to explore utilization.");
                LinearLayout scoreCard=column();scoreCard.setPadding(dp(18),dp(16),dp(18),dp(16));
                scoreCard.setBackground(shape(panel,gold,18));
                TextView scoreLabel=text("ILLUSTRATIVE SCORE INDEX",11,gold,true);scoreCard.addView(scoreLabel);
                TextView scoreValue=text(""+score(),40,white,true);scoreCard.addView(scoreValue);
                TextView utilization=text("Balance: $"+balance+"  •  Utilization: "+pct+"%",15,cyan,true);
                utilization.setPadding(0,dp(7),0,0);scoreCard.addView(utilization);
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.bottomMargin=dp(14);content.addView(scoreCard,cp);
                SeekBar slider=new SeekBar(this);slider.setMax(limit);slider.setProgress(balance);
                slider.setProgressTintList(ColorStateList.valueOf(cyan));
                slider.setThumbTintList(ColorStateList.valueOf(gold));
                slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                    public void onProgressChanged(SeekBar s,int p,boolean fromUser){
                        balance=p;scoreValue.setText(""+score());
                        utilization.setText("Balance: $"+balance+"  •  Utilization: "+(int)Math.round(100.0*balance/limit)+"%");
                    }
                    public void onStartTrackingTouch(SeekBar s){}
                    public void onStopTrackingTouch(SeekBar s){}
                });
                content.addView(slider);
                card("How to interpret this",note()+" A sample balance below $900 is under 30% of the $3,000 limit.");
                action("Reset simulation",()->{balance=960;show(5);});
                break;
        }
    }
}
