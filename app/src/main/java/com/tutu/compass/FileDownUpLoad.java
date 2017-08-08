package com.tutu.compass;


import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.FileCallback;

import java.io.File;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 文件下载上传管理器
 * Created by tutu on 2017/3/12.
 */

public class FileDownUpLoad {


    private static MaterialDialog dialog;

    /**
     * 下载文件
     */
    public static void downLoadFile(String tag, String url, String destFileDir, String name,
                                    final FileDownCallBackInterface downCallBackInterface) {//

        OkGo.get(url)
                .tag(tag)
                .cacheMode(CacheMode.IF_NONE_CACHE_REQUEST)    // 缓存模式，详细请看缓存介绍
                .execute(new FileCallback(destFileDir, name) {  //文件下载时，可以指定下载的文件目录和文件名
                    @Override
                    public void onSuccess(File file, Call call, Response response) {

                        // file 即为文件数据，文件保存在指定目录
                        downCallBackInterface.onSuccess(file);
                    }

                    @Override
                    public void downloadProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
                        //这里回调下载进度(该回调在主线程,可以直接更新ui)
                        downCallBackInterface.onProress(progress);
                    }

                    @Override
                    public void onCacheSuccess(File file, Call call) {

                        super.onCacheSuccess(file, call);
                        // file 即为文件数据，文件保存在指定目录
                        downCallBackInterface.onSuccess(file);

                    }

                    @Override
                    public void onCacheError(Call call, Exception e) {
                        super.onCacheError(call, e);

                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        downCallBackInterface.onFail(e);

                    }

                    @Override
                    public void onAfter(File file, Exception e) {
                        super.onAfter(file, e);

                    }
                });

    }


    public static void cancelRequest(String tag) {
        //根据 Tag 取消请求
        OkGo.getInstance().cancelTag(tag);


    }

    public static void cancelAllRequest() {
        //根据 Tag 取消请求
        OkGo.getInstance().cancelAll();

    }
}
