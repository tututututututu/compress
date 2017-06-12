package com.tutu.compass;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import me.shaohui.advancedluban.Luban;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_IMAGE = 1;

    private TextView tvPath;
    private NumberProgressBar npb;
    private EditText etPath;


    private ArrayList<String> filePathList = new ArrayList<>();
    RxPermissions rxPermissions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPath = (TextView) findViewById(R.id.tv_path);
        npb = (NumberProgressBar) findViewById(R.id.npb);
        etPath = (EditText) findViewById(R.id.et_path);
        rxPermissions = new RxPermissions(this);


        findViewById(R.id.compress).setOnClickListener(new View.OnClickListener() {
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
                                            .showCamera(true) // 是否显示相机. 默认为显示
                                            .count(9) // 最大选择图片数量, 默认为9. 只有在选择模式为多选时有效
                                            .single() // 单选模式
                                            .multi() // 多选模式, 默认模式;
                                            .origin(filePathList) // 默认已选择图片. 只有在选择模式为多选时有效
                                            .start(MainActivity.this, REQUEST_IMAGE);
                                } else {
                                    Toast.makeText(MainActivity.this.getApplicationContext(), "没有获取到权限", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                // 获取返回的图片列表
                filePathList = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                compress();
            }
        }
    }

    private void compress() {
        if (filePathList.isEmpty()) {
            return;
        }

        List<File> fileList = new ArrayList<>();

        for (String s : filePathList) {
            fileList.add(new File(s));
        }

        Luban.compress(MainActivity.this, fileList)
                .setMaxSize(500)                // 限制最终图片大小（单位：Kb）
                .setMaxHeight(1920)             // 限制图片高度
                .setMaxWidth(1080)              // 限制图片宽度
                .setCompressFormat(Bitmap.CompressFormat.JPEG)            // 自定义压缩图片格式，目前只支持：JEPG和WEBP，因为png不支持压缩图片品质
                .putGear(Luban.CUSTOM_GEAR)     // 使用 CUSTOM_GEAR 压缩模式
                .asListObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<File>>() {
                    @Override
                    public void accept(List<File> files) throws Exception {
                        tvPath.setText("");
                        if (!files.isEmpty()) {
                            for (File file : files) {
                                tvPath.setText(tvPath.getText() + "\n" + file.getAbsolutePath());
                            }
                            uploadImg(files);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        tvPath.setText(throwable.getMessage());
                        throwable.printStackTrace();
                    }
                });
    }


    public void uploadImg(List<File> fileList) {

        String path = etPath.getText().toString().trim();
        if (TextUtils.isEmpty(path)){
            Toast.makeText(MainActivity.this, "服务器路径不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        OkGo.<String>post(path)
                .tag(this)
                .isMultipart(true)       // 强制使用 multipart/form-data 表单上传（只是演示，不需要的话不要设置。默认就是false）
                .params("param", "vaule")        // 这里可以上传参数
                .addFileParams("key", fileList)    // 这里支持一个key传多个文件
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void uploadProgress(Progress progress) {
                        npb.setProgress((int) (progress.fraction * 100));
                    }

                    @Override
                    public void onError(Response<String> response) {
                        Toast.makeText(MainActivity.this, response.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
