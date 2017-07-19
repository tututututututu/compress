package com.tutu.compass;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import static android.view.KeyEvent.KEYCODE_BACK;
import static com.tutu.compass.R.id.webView;

public class WebViewActivity extends AppCompatActivity {
    private WebView mWebView;
    private View mProgressBar;
    private ImageView mErrorView;
    private ProgressBar pb;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);

        mWebView = (WebView) findViewById(webView);
        mProgressBar = findViewById(R.id.llpb);
        mErrorView = (ImageView) findViewById(R.id.iv_error);
        pb = (ProgressBar) findViewById(R.id.pb);

        pb.getIndeterminateDrawable().setColorFilter(Color.parseColor("#F36142"), PorterDuff.Mode.MULTIPLY);

        mWebView.addJavascriptInterface(new AndroidInterface(this), "android");



        mWebView.setWebChromeClient(new WebChromeClient() {
            //配置权限（同样在WebChromeClient中实现）
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                Log.e("webview", "progress=" + newProgress);
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                }


                if (!NetworkUtils.isConnected(WebViewActivity.this.getApplicationContext())) {
                    mProgressBar.setVisibility(View.GONE);
                    mErrorView.setImageResource(R.mipmap.neterror);
                    mErrorView.setVisibility(View.VISIBLE);
                    mErrorView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mWebView.reload();
                            mErrorView.setVisibility(View.GONE);
                        }
                    });
                    return;
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     final JsResult result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
                builder.setMessage(message)
                        .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        }).show();
                result.cancel();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url,
                                       String message, final JsResult result) {


                AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
                builder.setMessage(message)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                result.confirm();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                }).show();


                return true;
            }
        });


        //设置不用系统浏览器打开,直接显示在当前Webview
        mWebView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedHttpError(final WebView view, final WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);

                Log.e("webview", "onReceivedHttpError1" + errorResponse.getStatusCode());
                // 在这里显示自定义错误页
                if ((errorResponse.getStatusCode() == 404 || errorResponse.getStatusCode() == 109 || errorResponse.getStatusCode() == 102) && request.isForMainFrame()) {
                    Log.e("webview", "onReceivedHttpError2" + errorResponse.getStatusCode());
                    view.stopLoading();
                    mErrorView.setVisibility(View.VISIBLE);
                    mErrorView.setImageResource(R.mipmap.severror);
                    mErrorView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            view.loadUrl(request.getUrl().toString());
                            mProgressBar.setVisibility(View.VISIBLE);
                            mErrorView.setVisibility(View.GONE);
                        }
                    });
                }
            }

            // 旧版本，会在新版本中也可能被调用，所以加上一个判断，防止重复显示
            @Override
            public void onReceivedError(final WebView view, int errorCode, String description, final String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e("webview", "6.0- " + errorCode + " " + failingUrl);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return;
                }
                if (errorCode == -6 || errorCode == -2) {
                    Log.e("webview", "6.0- " + errorCode + " " + failingUrl);
                    view.stopLoading();
                    mErrorView.setVisibility(View.VISIBLE);
                    mErrorView.setImageResource(R.mipmap.severror);
                    mErrorView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            view.loadUrl(failingUrl);
                            mProgressBar.setVisibility(View.VISIBLE);
                            mErrorView.setVisibility(View.GONE);
                        }
                    });
                }
                // 在这里显示自定义错误页
            }

            // 新版本，只会在Android6及以上调用
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(final WebView view, final WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                Log.e("webview", "6.0+ " + error.toString() + " " + error.getErrorCode() + " " + request.getUrl().toString());
                // 在这里显示自定义错误页
                if (error.getErrorCode() == -6 || error.getErrorCode() == -2) {
                    Log.e("webview", "6.0+ " + error.toString() + " " + error.getErrorCode() + " " + request.getUrl().toString());
                    view.stopLoading();
                    mErrorView.setVisibility(View.VISIBLE);
                    mErrorView.setImageResource(R.mipmap.severror);
                    mErrorView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            view.loadUrl(request.getUrl().toString());
                            mProgressBar.setVisibility(View.VISIBLE);
                            mErrorView.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });


        //声明WebSettings子类
        WebSettings webSettings = mWebView.getSettings();

//如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);

//支持插件
        //webSettings.setPluginsEnabled(true);

//设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

//缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

//其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

        webSettings.setDatabaseEnabled(true);
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        webSettings.setGeolocationDatabasePath(dir);
        webSettings.setDomStorageEnabled(true);
        // 开启 Application Caches 功能
        webSettings.setAppCacheEnabled(true);

        mWebView.loadUrl(getUrl());

        if (!NetworkUtils.isConnected(WebViewActivity.this.getApplicationContext())) {
            mErrorView.setImageResource(R.mipmap.neterror);
            mErrorView.setVisibility(View.VISIBLE);
            mErrorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.reload();
                }
            });
            return;
        }
    }

    public String getUrl() {
        return Config.webUrl;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    //退出时的时间
    private long mExitTime;

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onDestroy() {
        //由于内核缓存是全局的因此这个方法不仅仅针对webview而是针对整个应用程序.
        mWebView.clearCache(true);
        mWebView.clearMatches();
        mWebView.clearFormData();

//清除当前webview访问的历史记录
//只会webview访问历史记录里的所有记录除了当前访问记录
        mWebView.clearHistory();

//这个api仅仅清除自动完成填充的表单数据，并不会清除WebView存储到本地的数据
        mWebView.clearFormData();

        //清空所有Cookie
        CookieSyncManager.createInstance(this.getApplicationContext());  //Create a singleton CookieSyncManager within a context
        CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
        cookieManager.removeAllCookie();// Removes all cookies.
        CookieSyncManager.getInstance().sync(); // forces sync manager to sync now

        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(null);
        mWebView.getSettings().setJavaScriptEnabled(false);
        mWebView.clearCache(true);
        super.onDestroy();

    }
}
