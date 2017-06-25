package com.tutu.compass;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etUrl;
    private View web;
    private View upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        etUrl = (EditText) findViewById(R.id.et_url);
        web = findViewById(R.id.btn_web);
        upload = findViewById(R.id.btn_upload_img);

        web.setOnClickListener(this);
        upload.setOnClickListener(this);

//        if (!SPUtils.getBoolean("noFirst")) {
            Intent intent = new Intent(StartActivity.this, FirstActivity.class);
            startActivity(intent);
            finish();
            SPUtils.putBoolean("noFirst", true);
//        } else {
//            findViewById(R.id.ll_root).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Intent intent = new Intent(StartActivity.this, WebViewActivity.class);
////                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                    SPUtils.putBoolean("noFirst", true);
//                }
//            }, 2000);
//        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_web:
                intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("url", etUrl.getText().toString().trim());
                break;
            case R.id.btn_upload_img:
                intent = new Intent(this, MainActivity.class);
                break;
        }
        startActivity(intent);
    }
}
