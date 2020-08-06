package com.tutu.compass;

import android.app.Application;

import com.lzy.okgo.OkGo;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by tutu on 2017/6/17.
 */

public class App extends Application {
    public static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "881830e041", false);
        app = this;
        SPUtils.initSP("data");
        OkGo.init(this);
    }
}
