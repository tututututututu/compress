package com.tutu.compass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_web:
                if (TextUtils.isEmpty(etUrl.getText().toString().trim())) {
                    Toast.makeText(this, "url为空,展示京东网页", Toast.LENGTH_LONG).show();
                }
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
