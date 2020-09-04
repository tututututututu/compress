package com.tutu.compass;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yzs.imageshowpickerview.ImageLoader;
import me.nereo.multi_image_selector.utils.FileUtils;

/**
 */

public class LoaderInterface extends ImageLoader {

    @Override
    public void displayImage(Context context, String path, ImageView imageView) {
        if (path.startsWith("/")){
            Glide.with(context).load(FileUtils.getImageContentUri(context,path)).into(imageView);
        }else {
            Glide.with(context).load(path).into(imageView);
        }

    }

    @Override
    public void displayImage(Context context, @DrawableRes Integer resId, ImageView imageView) {
        imageView.setImageResource(resId);
    }

}
