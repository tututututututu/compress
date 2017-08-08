package com.tutu.compass;

import android.app.Application;

import com.lzy.okgo.OkGo;

/**
 * Created by tutu on 2017/6/17.
 */

public class App extends Application {
    public static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        SPUtils.initSP("data");
        OkGo.init(this);
    }
}
