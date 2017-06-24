package com.tutu.compass;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.request.BaseRequest;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzs.imageshowpickerview.ImageShowPickerBean;
import com.yzs.imageshowpickerview.ImageShowPickerListener;
import com.yzs.imageshowpickerview.ImageShowPickerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import me.shaohui.advancedluban.Luban;
import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_IMAGE = 1;

    private List<File> selectFiles = new ArrayList<>();
    private List<File> compressFiles = new ArrayList<>();
    private ArrayList<String> selectFilePath = new ArrayList<>();
    private List<ImageBean> listImg = new ArrayList<>();
    RxPermissions rxPermissions;


    ImageShowPickerView pickerView;
    TextView tvUpload;
    LinearLayout llBack;
    private MaterialDialog materialDialog;
    TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickerView = (ImageShowPickerView) findViewById(R.id.it_picker_view);
        llBack = (LinearLayout) findViewById(R.id.ll_back);
        tvUpload = (TextView) findViewById(R.id.tv_upload);
        pickerView.setShowAnim(true);
        pickerView.setMaxNum(Config.maxCount);
        tvInfo = (TextView) findViewById(R.id.tv_info);

        pickerView.setPickerListener(new ImageShowPickerListener() {
            @Override
            public void addOnClickListener(int remainNum) {
                pick();
            }

            @Override
            public void picOnClickListener(List<ImageShowPickerBean> list, int position, int remainNum) {

            }

            @Override
            public void delOnClickListener(int position, int remainNum) {
                listImg.remove(position);
                selectFilePath.remove(position);
                selectFiles.remove(position);
            }
        });
        pickerView.setImageLoaderInterface(new LoaderInterface());
        pickerView.setNewData(listImg);
        pickerView.show();

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compress();
            }
        });
    }


    private void pick() {
        rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            MultiImageSelector.create()
                                    .count(30)
                                    .origin(selectFilePath)
                                    .start(MainActivity.this, REQUEST_IMAGE);
                        } else {
                            showToast("没有获取到权限");
                        }
                    }
                });
    }


    /**
     * 压缩文件
     */
    public void compress() {
        if (selectFiles.isEmpty()) {
            return;
        }

        showProgress();
        Luban.compress(selectFiles, new File(Path.getAppCachePath()))
                .putGear(Luban.CUSTOM_GEAR)
                .setMaxSize(Config.maxSize)
                .setMaxHeight(Config.maxHeight)
                .setMaxWidth(Config.maxWidth)
                .asListObservable()
                .subscribe(new Consumer<List<File>>() {
                    @Override
                    public void accept(List<File> files) throws Exception {
                        compressFiles.clear();
                        compressFiles = files;
                        uploadImg(compressFiles);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    private void showProgress() {
        materialDialog = new MaterialDialog.Builder(this)
                .title("处理中...")
                .progress(true, 0)
                .show();
    }

    private void updateProgress(String content) {
        materialDialog.setTitle(content);
    }

    private void cancelProgress() {
        tvUpload.post(new Runnable() {
            @Override
            public void run() {
                materialDialog.cancel();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE && data != null) {
            selectFiles.clear();
            listImg.clear();
            selectFilePath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            for (String str : selectFilePath) {
                selectFiles.add(new File(str));
                listImg.add(new ImageBean(str));
            }

            pickerView.setNewData(listImg);
            pickerView.show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void uploadImg(List<File> fileList) {

        if (TextUtils.isEmpty(Config.imgUploadPath)) {
            showToast("服务器路径不能为空");
            return;
        }

        for (File file : fileList) {
            if (!file.exists()) {
                showToast("file.getAbsolutePath() + \"  不存在\"");
                return;
            }
        }
        updateProgress("上传中...");
        try {
            OkGo.<BaseRespBean>post(Config.imgUploadPath)
                    .tag(this)//
                    .isMultipart(true)
                    .addFileParams("file[]", fileList)
                    .params("appKey", Config.APPKEY)
                    .params("ordersid", Config.ordersid)
                    .execute(new AbsCallback<BaseRespBean>() {
                        @Override
                        public BaseRespBean convertSuccess(final Response response) throws Exception {
                            try {
                                tvUpload.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            tvInfo.setText(tvInfo.getText().toString() + "服务端返回数据" + response.body().string() + "\n");
                                        }catch (Exception e){
                                        }

                                    }
                                });
                                //tvInfo.setText(tvInfo.getText().toString()+"解析jison"+" "+JSON.parseObject(response.body().string(), BaseRespBean.class)+"\n");
                                return JSON.parseObject(response.body().string(), BaseRespBean.class);
                            } catch (Exception e) {
                                //tvInfo.setText(tvInfo.getText().toString()+" "+"解析jison失败"+e.getMessage()+"\n");
                                cancelProgress();
                                showToast(e.getMessage());
                            }
                            return null;
                        }

                        @Override
                        public void onSuccess(BaseRespBean s, Call call, okhttp3.Response response) {
                            cancelProgress();
                        }

                        @Override
                        public void upProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
                            super.upProgress(currentSize, totalSize, progress, networkSpeed);
                            Log.e("tutu", progress + "");
                            tvInfo.setText(tvInfo.getText().toString() + " " + "上传进度" + progress + "\n");
                        }

                        @Override
                        public void onAfter(BaseRespBean s, Exception e) {
                            if (s == null) {
                                tvInfo.setText(tvInfo.getText().toString() + " " + "上传完成 返回json转的对象为空" + "\n");
                                cancelProgress();
                                return;
                            }
                            tvInfo.setText(tvInfo.getText().toString() + " " + "解析对象成功 返回json转的对象为" + s.toString() + "\n");
                            super.onAfter(s, e);
                            if (s.getCode().equals("1")) {
                                tvInfo.setText(tvInfo.getText().toString() + " " + "上传成功 code=" + s.getCode() + "\n");
                                clearData();
                            } else {
                                tvInfo.setText(tvInfo.getText().toString() + " " + "上传失败 code=" + s.getCode() + "\n");
                                showToast(s.getMsg());
                            }

                            cancelProgress();
                        }

                        @Override
                        public void onBefore(BaseRequest request) {
                            super.onBefore(request);
                        }

                        @Override
                        public void onCacheError(Call call, Exception e) {
                            super.onCacheError(call, e);
                        }

                        @Override
                        public void onCacheSuccess(BaseRespBean s, Call call) {
                            super.onCacheSuccess(s, call);
                        }

                        @Override
                        public void onError(Call call, okhttp3.Response response, Exception e) {
                            super.onError(call, response, e);
                            showToast(e.getMessage());
                            cancelProgress();
                        }
                    });

        } catch (Exception e) {
            showToast(e.getMessage());
            cancelProgress();
        }
    }

    public void showToast(final String msg) {
        if (!TextUtils.isEmpty(msg)) {
            tvUpload.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    /**
     * 文件上传完毕  清除数据
     */
    private void clearData() {
        selectFilePath.clear();
        selectFiles.clear();
        compressFiles.clear();
        showToast("上传成功");
        finish();
    }
}
