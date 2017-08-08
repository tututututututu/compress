package com.tutu.compass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.just.library.BaseIndicatorView;
import com.tutu.compass.utils.NetworkUtils;

/**
 * Created by tutu on 2017/6/25.
 */

public class CommonIndicator extends BaseIndicatorView {
    public CommonIndicator(Context context) {
        super(context);
        setBackground(new ColorDrawable(Color.parseColor("#55000000")));
    }

    public CommonIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CommonIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void show() {
        if (!NetworkUtils.isConnected(getContext().getApplicationContext())){
            getContext().startActivity(new Intent(getContext(), NetErrorActivity.class));
            ((Activity)getContext()).finish();
            return;
        }
        this.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        this.setVisibility(View.GONE);
    }


    @Override
    public LayoutParams offerLayoutParams() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
