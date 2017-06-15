package com.tutu.compass;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.just.library.AgentWeb;
import com.just.library.ChromeClientCallbackManager;
import com.just.library.LogUtils;

public class WebViewActivity extends AppCompatActivity {

    protected AgentWeb mAgentWeb;
    private LinearLayout mLinearLayout;
    private String url;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);

        url = getIntent().getStringExtra("url");


        mLinearLayout = (LinearLayout) this.findViewById(R.id.container);

        mAgentWeb = AgentWeb.with(this)//
                .setAgentWebParent(mLinearLayout, new LinearLayout.LayoutParams(-1, -1))//
                .useDefaultIndicator()//
                .defaultProgressBarColor()
                .setReceivedTitleCallback(mCallback)
                .setSecutityType(AgentWeb.SecurityType.strict)
                .createAgentWeb()//
                .ready()
                .go(null);

        mAgentWeb.getLoader().loadUrl(getUrl());

        mAgentWeb.getJsInterfaceHolder().addJavaObject("android", new AndroidInterface(mAgentWeb, this));
    }

    public String getUrl() {
        return url;
    }

    private ChromeClientCallbackManager.ReceivedTitleCallback mCallback = new ChromeClientCallbackManager.ReceivedTitleCallback() {
        @Override
        public void onReceivedTitle(WebView view, String title) {

        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        LogUtils.i("Info", "result:" + requestCode + " result:" + resultCode);
        mAgentWeb.uploadFileResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mAgentWeb.destroy();
        mAgentWeb.getWebLifeCycle().onDestroy();
    }
}
