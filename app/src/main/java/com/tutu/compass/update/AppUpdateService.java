package com.tutu.compass.update;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.tutu.compass.App;
import com.tutu.compass.utils.AppUtils;
import com.tutu.compass.CacheManager;
import com.tutu.compass.Config;
import com.tutu.compass.FileDownCallBackInterface;
import com.tutu.compass.FileDownUpLoad;
import com.tutu.compass.utils.GsonUtils;
import com.tutu.compass.Path;
import com.tutu.compass.ToastUtils;
import com.tutu.compass.UpdateBean;
import com.tutu.compass.VersionInfoBean;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Response;


/**
 * 应用更新服务
 * Created by tutu on 2017/3/16.
 */

public class AppUpdateService {

    //提示框
    private static MaterialDialog dialog;

    //下载进度框
    private static MaterialDialog downDialog;

    /**
     * 外部直接调用这个方法
     */
    public static void updateService(final Activity activity) {
        OkGo.getInstance().debug("update").<UpdateBean>post(Config.updateUrl)
                .isMultipart(true)
                .params("versionCode", Path.getVersionCode(App.app))
                .params("appKey", Config.APPKEY)
                .execute(new AbsCallback<UpdateBean>() {
                    @Override
                    public UpdateBean convertSuccess(final Response response) throws Exception {
                        return GsonUtils.jsonToObj(response.body().string(), UpdateBean.class);
                    }

                    @Override
                    public void onSuccess(UpdateBean baseRespBean, Call call, Response response) {
                        //测试代码
                        Log.e("update",baseRespBean.toString());
//                        baseRespBean.getUpdateInfo().setHasUpdate(true);
//                        baseRespBean.getUpdateInfo().setUrl("http://400.6805685.com/mycrm/andrpack/StaffAssistantDebug.apk");
                        Log.e("update",baseRespBean.toString());

                        VersionInfoBean lUpdateInfoBean = new VersionInfoBean();
                        if (baseRespBean.getUpdateInfo().isHasUpdate()) {
                            lUpdateInfoBean.setNeedUpdate(true);

                            if (baseRespBean.getUpdateInfo().isForce()) {
                                lUpdateInfoBean.setForce(true);
                            } else {
                                lUpdateInfoBean.setForce(false);
                            }

                            lUpdateInfoBean.setServerVersion(baseRespBean.getUpdateInfo().getVersionName());
                            lUpdateInfoBean.setUpdateMsg(baseRespBean.getUpdateInfo().getUpdateContent());
                            lUpdateInfoBean.setUrl(baseRespBean.getUpdateInfo().getUrl());
                        } else {
                            lUpdateInfoBean.setNeedUpdate(false);
                        }


                        updateCheck(lUpdateInfoBean, activity);
                        return;
                    }


                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        ToastUtils.showShortToast(e.getMessage());
                    }
                });

    }

    private static void updateCheck(VersionInfoBean versionInfo, Activity activity) {

        if (versionInfo.isNeedUpdate()) {

            if (TextUtils.isEmpty(versionInfo.getUrl())) {

                ToastUtils.showShortToast("下载链接错误");
                return;
            }

            //强制更新
            if ("1".equals(versionInfo.isForce())) {
                showUpdateDialog(versionInfo, new WeakReference<>(activity));
            } else {
                //非强制更新
                showUpdateDialog(versionInfo, new WeakReference<>(activity));
            }

        }
    }

    /**
     * 显示更新提示框
     *
     * @param versionInfo 版本信息
     */
    private static void showUpdateDialog(final VersionInfoBean versionInfo, final WeakReference<Activity> activity) {
        String cancelStr = "取消";
        if (versionInfo.isForce()) {
            cancelStr = "退出";
        }

        if (activity.get() == null) {
            return;
        }

        dialog = new MaterialDialog.Builder(activity.get()).positiveText("更新").cancelable(false)
                .title("有新版本可更新")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showDownLoadDialog(activity.get());
                        cancelTipsDialog();
                        downApk(versionInfo, activity);
                    }
                })
                .negativeText(cancelStr)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (versionInfo.isForce()) {
                            activity.get().finish();
                        } else {
                            cancelTipsDialog();
                        }
                    }
                })
                .build();

        dialog.setContent(versionInfo.getUpdateMsg());
        dialog.show();
    }


    /**
     * 下载apk文件
     *
     * @param versionInfo 版本信息
     * @param activity    activity
     */
    private static void downApk(VersionInfoBean versionInfo, final WeakReference<Activity> activity) {
        FileDownUpLoad.downLoadFile("downApk",
                versionInfo.getUrl()
                , getNewApkFileDir(), getNewApkName(String.valueOf(versionInfo.getServerVersion()))
                , new FileDownCallBackInterface() {
                    @Override
                    public void onProress(float progress) {
                        downDialog.setProgress((int) (progress * 100));
                    }

                    @Override
                    public void onSuccess(File file) {
                        downFileSuccess(file, activity.get());
                    }

                    @Override
                    public void onFail(Exception e) {
                        cancelDownDialog();
                        cancelTipsDialog();
                    }
                }
        );
    }

    /**
     * 下载文件成功 以及安装
     * 会消失掉下载框和提示框
     *
     * @param file     文件file
     * @param activity activity
     */
    private static void downFileSuccess(File file, Activity activity) {
        chekApk(file);
        cancelDownDialog();
        cancelTipsDialog();
        if (activity == null) {
            return;
        }
        AppUtils.installApp(activity, file);
    }

    /**
     * 检查文件是否可用
     *
     * @param file 下载的文件file
     */
    private static void chekApk(File file) {
        if (!file.exists()) {
            ToastUtils.showShortToast("没有找到安装包");
        }
    }


    /**
     * 显示下载进度
     *
     * @param activity
     */
    private static void showDownLoadDialog(Activity activity) {
        if (activity == null) {
            return;
        }
        downDialog = new MaterialDialog.Builder(activity)
                .cancelable(false)
                .title("下载中...")
                .progress(false, 100)
                .build();
        downDialog.show();
    }

    /**
     * 取消下载进度
     */
    private static void cancelDownDialog() {
        if (downDialog != null) {
            downDialog.cancel();
        }
    }

    /**
     * 取消提示框
     */
    private static void cancelTipsDialog() {
        if (dialog != null) {
            dialog.cancel();
        }
    }

    /**
     * 获取下载app的路径文件夹
     *
     * @return
     */
    public static String getNewApkFileDir() {
        return CacheManager.getTyleCachePath(CacheManager.COMMON_CACHE_PATH);
    }

    /**
     * 新下载app的文件名
     *
     * @param version
     * @return
     */
    public static String getNewApkName(String version) {
        if (TextUtils.isEmpty(version)) {
            version = new Date().getTime() + "";
        }
        return "starff" + version + ".apk";
    }
}
