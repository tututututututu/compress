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
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickerView = (ImageShowPickerView) findViewById(R.id.it_picker_view);
        llBack = (LinearLayout) findViewById(R.id.ll_back);
        tvUpload = (TextView) findViewById(R.id.tv_upload);
        pickerView.setShowAnim(true);
        pickerView.setMaxNum(30);

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
                            Toast.makeText(MainActivity.this.getApplicationContext(), "没有获取到权限", Toast.LENGTH_SHORT).show();
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
        materialDialog.cancel();
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
            Toast.makeText(MainActivity.this, "服务器路径不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        for (File file : fileList) {
            if (!file.exists()) {
                Toast.makeText(this, file.getAbsolutePath() + "  不存在", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        updateProgress("上传中...");
        try {
            OkGo.post(Config.imgUploadPath)
                    .tag(this)//
                    .isMultipart(true)
                    .addFileParams("file[]", fileList)
                    .params("appKey", Config.APPKEY)
                    .params("ordersid", Config.ordersid)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, okhttp3.Response response) {
                            Log.e("tutu", s);
                        }

                        @Override
                        public void upProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
                            super.upProgress(currentSize, totalSize, progress, networkSpeed);
                            Log.e("tutu", progress + "");
                        }

                        @Override
                        public void onAfter(String s, Exception e) {
                            super.onAfter(s, e);
                            clearData();
                            cancelProgress();
                        }

                        @Override
                        public void onBefore(BaseRequest request) {
                            super.onBefore(request);
                            Log.e("tutu", request.toString());
                        }

                        @Override
                        public void onCacheError(Call call, Exception e) {
                            super.onCacheError(call, e);
                        }

                        @Override
                        public void onCacheSuccess(String s, Call call) {
                            super.onCacheSuccess(s, call);
                        }

                        @Override
                        public void onError(Call call, okhttp3.Response response, Exception e) {
                            super.onError(call, response, e);
                            cancelProgress();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            cancelProgress();
        }
    }


    /**
     * 文件上传完毕  清除数据
     */
    private void clearData() {
        selectFilePath.clear();
        selectFiles.clear();
        compressFiles.clear();
        finish();
    }
}
