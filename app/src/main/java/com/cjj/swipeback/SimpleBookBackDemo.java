package com.cjj.swipeback;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjj.SwipeBackLayout;

/**
 * Created by Administrator on 2015/11/17.
 */
public class SimpleBookBackDemo extends AppCompatActivity {
    private SwipeBackLayout mSwipeBackLayout;
    private TextView tv;
    private RelativeLayout rl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_book);
        rl = (RelativeLayout) findViewById(R.id.rl);
        tv = (TextView) findViewById(R.id.tv);
        tv.animate().rotation(-90);
        mSwipeBackLayout = (SwipeBackLayout) findViewById(R.id.sb);
        mSwipeBackLayout.setOnSwipeBackListener(new SwipeBackLayout.SwipeBackListener() {
            @Override
            public void onOpen() {
                finish();
                overridePendingTransition(0, R.anim.right_anim);
            }

            @Override
            public void onClose() {

            }

            @Override
            public void onSwipe(float percent) {
                rl.setTranslationX(percent * 30 - 30);
                tv.setAlpha(percent<0.5?0.1f:percent);
            }
        });
    }

}
