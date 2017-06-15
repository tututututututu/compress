package com.tutu.compass;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.just.library.AgentWeb;

/**
 * Created by tutu on 2017/6/15.
 */

public class AndroidInterface {

    private AgentWeb agent;
    private Context context;

    public AndroidInterface(AgentWeb agent, Context context) {
        this.agent = agent;
        this.context = context;
    }

    private Handler deliver = new Handler(Looper.getMainLooper());

    @JavascriptInterface
    public void upLoadImg(final String msg) {

        deliver.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), "一个参数的方法被调用:" + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @JavascriptInterface
    public void upLoadImg() {
        deliver.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), "无参方法被调用了", Toast.LENGTH_LONG).show();
            }
        });
    }

}
