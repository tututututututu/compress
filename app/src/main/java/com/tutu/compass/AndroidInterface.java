package com.tutu.compass;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by tutu on 2017/6/15.
 */

public class AndroidInterface {

    private Context context;

    public AndroidInterface(Context context) {

        this.context = context;
    }

    private Handler deliver = new Handler(Looper.getMainLooper());

    @JavascriptInterface
    public void upLoadImg(final String maxCount, final String maxSize, final String maxWidth, final String maxHeight, final String ordersid) {

        deliver.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Config.maxCount = Integer.parseInt(maxCount);
                    Config.maxSize = Integer.parseInt(maxSize);
                    Config.maxWidth = Integer.parseInt(maxWidth);
                    Config.maxHeight = Integer.parseInt(maxHeight);
                    Config.ordersid = ordersid;
                    if (TextUtils.isEmpty(ordersid)) {
                        Toast.makeText(context.getApplicationContext(), "ordersid为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (Config.maxCount < 1) {
                        Toast.makeText(context.getApplicationContext(), "图片数量不能小于1", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "图片参数设置错误", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @JavascriptInterface
    public void upLoadImg(final String ordersid, final String roomtypeid, final String ext, final String size) {
        deliver.post(new Runnable() {
            @Override
            public void run() {
                Log.e("js", "三个参数的");
                if (TextUtils.isEmpty(ordersid)) {
                    Toast.makeText(context.getApplicationContext(), "ordersid为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Config.ordersid = ordersid;
                    Config.roomtypeid = roomtypeid;
                    Config.ext = ext;
                    Config.maxSize = Integer.parseInt(size);
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context.getApplicationContext(), "js数据错误", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    @JavascriptInterface
    public void upLoadImg(final String ordersid) {
        deliver.post(new Runnable() {
            @Override
            public void run() {
                Log.e("js", "一个参数的");
                if (TextUtils.isEmpty(ordersid)) {
                    Toast.makeText(context.getApplicationContext(), "ordersid为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Config.ordersid = ordersid;
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        });
    }
}
