package com.tutu.compass;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            builder.detectAll();
            StrictMode.setVmPolicy(builder.build());
        }
        SPUtils.initSP("data");
        OkGo.init(this);
    }
}
