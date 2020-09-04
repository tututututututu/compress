package com.tutu.compass;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.bugly.crashreport.CrashReport;
import com.yzs.imageshowpickerview.ImageShowPickerBean;
import com.yzs.imageshowpickerview.ImageShowPickerListener;
import com.yzs.imageshowpickerview.ImageShowPickerView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.internal.utils.PathUtils;

import java.io.File;
import java.io.IOException;
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

    int count = 1;

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

        tvInfo.setText("ordersid=" + Config.ordersid);
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


                            Matisse.from(MainActivity.this)
                                    .choose(MimeType.ofImage())
                                    .countable(true)
                                    .maxSelectable(30)
                                    .capture(true)
                                    .captureStrategy(new CaptureStrategy(true, "com.tutu.compass.fileProvider"))
                                    .showSingleMediaType(true)
                                    .theme(R.style.Matisse_Dracula)
                                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                    .thumbnailScale(0.85f)
                                    .imageEngine(new MyGlideEngine())
                                    .forResult(REQUEST_IMAGE);

//                            MultiImageSelector.create()
//                                    .count(30)
//                                    .origin(selectFilePath)
//                                    .start(MainActivity.this, REQUEST_IMAGE);
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

                        materialDialog.setTitle("上传中...");
                        updateProgress(1 + "");

                        for (File compressFile : compressFiles) {
                            uploadImg(compressFile);
                        }

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
                .cancelable(false)
                .progress(true, 0)
                .show();

    }

    private void updateProgress(String content) {
        materialDialog.setContent("第" + content + "/" + compressFiles.size() + "张");
    }

    private void cancelProgress() {
        tvUpload.post(new Runnable() {
            @Override
            public void run() {
                materialDialog.cancel();
            }
        });

    }

    /**
     * 根据Uri获取文件真实地址
     */
    public static String getRealFilePath(Context context, Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String realPath = null;
        if (scheme == null)
            realPath = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            realPath = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA},
                    null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        realPath = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        if (TextUtils.isEmpty(realPath)) {
            if (uri != null) {
                String uriString = uri.toString();
                int index = uriString.lastIndexOf("/");
                String imageName = uriString.substring(index);
                File storageDir;

                storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File file = new File(storageDir, imageName);
                if (file.exists()) {
                    realPath = file.getAbsolutePath();
                } else {
                    storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File file1 = new File(storageDir, imageName);
                    realPath = file1.getAbsolutePath();
                }
            }
        }
        return realPath;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            selectFiles.clear();
            listImg.clear();

            List<Uri> mSelected = Matisse.obtainResult(data);
            if (mSelected != null) {
                for (int i = 0; i < mSelected.size(); i++) {
                    String filePath;
                    try {
                        filePath = PathUtils.getPath(this, mSelected.get(i));
                    } catch (Exception e) {
                        filePath = getRealFilePath(this, mSelected.get(i));
                    }

                    selectFilePath.add(filePath);
                    selectFiles.add(new File(filePath));
                    listImg.add(new ImageBean(filePath));
                }
            }


//            selectFilePath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
//            for (String str : selectFilePath) {
//                selectFiles.add(new File(str));
//                listImg.add(new ImageBean(str));
//            }

            pickerView.setNewData(listImg);
            pickerView.show();
        }
    }


    public void uploadImg(File file) {

        if (TextUtils.isEmpty(Config.imgUploadPath)) {
            showToast("服务器路径不能为空");
            cancelProgress();
            count = 0;
            return;
        }


        if (!file.exists()) {
            showToast("file.getAbsolutePath() + \"  不存在\"");
            cancelProgress();
            count = 0;
            return;
        }


        try {
            OkGo.<BaseRespBean>post(Config.imgUploadPath + "?ordersid=" + Config.ordersid + "&" + "roomtypeid=" + Config.roomtypeid + "&ext=" + Config.ext)
                    .tag(this)//
                    .isMultipart(true)
                    .params("file", file)
                    .params("appKey", Config.APPKEY)
//                    .params("roomtypeid", Config.roomtypeid)
//                    .params("ext", Config.ext)
                    //.params("ordersid", Config.ordersid)
                    .execute(new AbsCallback<BaseRespBean>() {

                        @Override
                        public BaseRespBean convertSuccess(final Response response) throws Exception {
                            tvInfo.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        tvInfo.setText(tvInfo.getText().toString() + response.body().string() + "\n");
                                    } catch (IOException e) {
                                        tvInfo.setText(tvInfo.getText().toString() + e.getMessage() + "\n");
                                    }
                                }
                            });
                            return null;
                        }

                        @Override
                        public void onSuccess(BaseRespBean baseRespBean, Call call, Response response) {
                            tvInfo.setText(tvInfo.getText().toString() + "第" + count + "张上传成功" + "\n");
                            if (count == compressFiles.size()) {
                                tvInfo.setText(tvInfo.getText().toString() + "所有文件上传完成" + "\n");
                                /**
                                 * 上传完毕
                                 */
                                cancelProgress();
                                clearData();
                                finish();
                            } else {
                                count++;
                                updateProgress(count + "");
                            }
                        }

                        @Override
                        public void onError(Call call, Response response, Exception e) {
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
        count = 0;
        selectFilePath.clear();
        selectFiles.clear();
//        compressFiles.clear();
        showToast("上传成功");
        // finish();
    }
}
