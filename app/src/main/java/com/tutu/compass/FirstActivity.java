package com.tutu.compass;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.bugly.crashreport.CrashReport;
import com.tutu.compass.update.AppUpdateService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import me.relex.circleindicator.CircleIndicator;

public class FirstActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private ViewPager vp;
    private int[] imageIdArray;//图片资源的数组
    private List<View> viewList;//图片资源的集合


    //最后一页的按钮
    private Button ib_start;
    private CircleIndicator indicator;
    private RxPermissions rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateRequest();
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_first);
        indicator = (CircleIndicator) findViewById(R.id.indicator);
        rxPermissions = new RxPermissions(this);

        ib_start = (Button) findViewById(R.id.guide_ib_start);
        ib_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (!NetworkUtils.isConnected(FirstActivity.this.getApplicationContext())) {
//                    startActivity(new Intent(FirstActivity.this, NetErrorActivity.class));
//                    finish();
//                    return;
//                }

//                startActivity(new Intent(FirstActivity.this, MainActivity.class));

                rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    startActivity(new Intent(FirstActivity.this, WebViewActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(FirstActivity.this.getApplicationContext(), "没有获取到权限无法使用", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        //加载ViewPager
        initViewPager();
    }

    /**
     * 加载图片ViewPager
     */
    private void initViewPager() {
        vp = (ViewPager) findViewById(R.id.guide_vp);
        //实例化图片资源
        imageIdArray = new int[]{R.drawable.splash1, R.drawable.splash2, R.drawable.splash3};
        viewList = new ArrayList<>();
        //获取一个Layout参数，设置为全屏
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        //循环创建View并加入到集合中
        int len = imageIdArray.length;
        for (int i = 0; i < len; i++) {
            //new ImageView并设置全屏和图片资源
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(params);
            imageView.setImageResource(imageIdArray[i]);

            //将ImageView加入到集合中
            viewList.add(imageView);
        }

        //View集合初始化好后，设置Adapter
        vp.setAdapter(new GuidePageAdapter(viewList));
        //设置滑动监听
        vp.setOnPageChangeListener(this);

        indicator.setViewPager(vp);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * 滑动后的监听
     *
     * @param position
     */
    @Override
    public void onPageSelected(int position) {

        //判断是否是最后一页，若是则显示按钮
        if (position == imageIdArray.length - 1) {
            ib_start.setVisibility(View.VISIBLE);
        } else {
            ib_start.setVisibility(View.GONE);
        }
    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }

    private void updateRequest() {

        AppUpdateService.updateService(this);
//        UpdateManager.create(FirstActivity.this)
//                .setUrl(Config.updateUrl)
//                .setPostData("versionCode=" + getVersionCode(FirstActivity.this) + "&appKey=" + Config.APPKEY)
//                .setParser(new IUpdateParser() {
//                    @Override
//                    public UpdateInfo parse(String source) throws Exception {
//
//                        Log.e("update", source);
//                        UpdateBean updateBean = JSON.parseObject(source, UpdateBean.class);
//                        return updateBean.getUpdateInfo();
//                    }
//                })
//                .setWifiOnly(false)
//                .check();


    }

    public void showToast(final String msg) {
        if (!TextUtils.isEmpty(msg)) {
            vp.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FirstActivity.this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
