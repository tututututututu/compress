package com.tutu.compass;

import android.os.Environment;

/**
 * Created by tutu on 2017/6/17.
 */

public class Path {
    /**
     * 获取app缓存目录
     *
     * @return
     */
    public static String getAppCachePath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return App.app.getExternalCacheDir().getAbsolutePath();
        } else {
            return App.app.getCacheDir().getAbsolutePath();
        }
    }
}
