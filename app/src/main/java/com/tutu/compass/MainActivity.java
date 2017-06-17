package com.tutu.compass;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.BaseRequest;
import com.tbruyelle.rxpermissions2.RxPermissions;

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

    private TextView tvPath;
    private NumberProgressBar npb;
    private EditText etPath;
    private ImageView ivImg;


    private List<File> selectFiles = new ArrayList<>();
    private List<File> compressFiles = new ArrayList<>();
    RxPermissions rxPermissions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPath = (TextView) findViewById(R.id.tv_path);
        npb = (NumberProgressBar) findViewById(R.id.npb);
        etPath = (EditText) findViewById(R.id.et_path);
        ivImg = (ImageView) findViewById(R.id.iv);
        rxPermissions = new RxPermissions(this);


        findViewById(R.id.select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rxPermissions.request(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    MultiImageSelector.create()
//                                            .showCamera(true) // 是否显示相机. 默认为显示
//                                            .count(9) // 最大选择图片数量, 默认为9. 只有在选择模式为多选时有效
//                                            //.single() // 单选模式
//                                            .multi() // 多选模式, 默认模式;
//                                            .origin(filePathList) // 默认已选择图片. 只有在选择模式为多选时有效
                                            .start(MainActivity.this, REQUEST_IMAGE);
                                } else {
                                    Toast.makeText(MainActivity.this.getApplicationContext(), "没有获取到权限", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        findViewById(R.id.compress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectFiles.isEmpty()) {
                    return;
                }

                tvPath.setText("");
                Luban.compress(selectFiles, new File(Path.getAppCachePath()))
                        .putGear(Luban.CUSTOM_GEAR)
                        .setMaxSize(500)
                        .setMaxHeight(1920)
                        .setMaxWidth(1080)
                        .asListObservable()
                        .subscribe(new Consumer<List<File>>() {
                            @Override
                            public void accept(List<File> files) throws Exception {
                                compressFiles.clear();
                                compressFiles = files;
                                int size = files.size();
                                while (size-- > 0) {
                                    tvPath.setText(tvPath.getText() + "\n" + files.get(size).getAbsolutePath());
                                    ivImg.setImageURI(Uri.fromFile(files.get(size)));
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });
            }
        });


        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImg(compressFiles);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        tvPath.setText("");

        if (requestCode == REQUEST_IMAGE && data != null) {
            selectFiles.clear();
            List<String> path = data
                    .getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            for (String str : path) {
                selectFiles.add(new File(str));
                tvPath.setText(tvPath.getText().toString().trim() + "\n" + str);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void uploadImg(List<File> fileList) {

        String path = etPath.getText().toString().trim();
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(MainActivity.this, "服务器路径不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        for (File file : fileList) {
            if (!file.exists()) {
                Toast.makeText(this, file.getAbsolutePath() + "  不存在", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        tvPath.setText("");
        try {
            OkGo.post(path)//
                    .tag(this)//
//                .isMultipart(true)       // 强制使用 multipart/form-data 表单上传（只是演示，不需要的话不要设置。默认就是false）
                    //.params("file1", fileList.get(0))   // 可以添加文件上传
                    .addFileParams("file", fileList)
                    //.params(params)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, okhttp3.Response response) {
                            tvPath.setText(tvPath.getText().toString().trim() + "\n" + "上传成功回调 " + s);
                            Log.e("tutu", s);
                        }

                        @Override
                        public void upProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
                            super.upProgress(currentSize, totalSize, progress, networkSpeed);
                            tvPath.setText(tvPath.getText().toString().trim() + "\n" + "进度回调 " + progress);
                            Log.e("tutu", progress + "");
                        }

                        @Override
                        public void onAfter(String s, Exception e) {
                            super.onAfter(s, e);
                            tvPath.setText(tvPath.getText().toString().trim() + "\n" + "请求完成回调 " + s);
                            Log.e("tutu", s);
                        }

                        @Override
                        public void onBefore(BaseRequest request) {
                            super.onBefore(request);
                            tvPath.setText(tvPath.getText().toString().trim() + "\n" + "上传前回调 " + request.toString());
                            Log.e("tutu", request.toString());
                        }

                        @Override
                        public void onCacheError(Call call, Exception e) {
                            super.onCacheError(call, e);
                            tvPath.setText(tvPath.getText().toString().trim() + "\n" + "缓存错误回调 " + e.toString());
                        }

                        @Override
                        public void onCacheSuccess(String s, Call call) {
                            super.onCacheSuccess(s, call);
                            tvPath.setText(tvPath.getText().toString().trim() + "\n" + "缓存成功回调 " + s);
                        }

                        @Override
                        public void onError(Call call, okhttp3.Response response, Exception e) {
                            super.onError(call, response, e);
                            tvPath.setText(tvPath.getText().toString().trim() + "\n" + "错误回调 " + e.toString());
                            Log.e("tutu", e.toString());
                        }
                    });

        } catch (Exception e) {
            tvPath.setText(e.getMessage());
        }
    }

}
