package com.tutu.compass;

/**
 * Created by tutu on 2017/7/28.
 */

public class UpdateInfo {
    /**
     * hasUpdate : true
     * isSilent : false
     * isForce : true
     * isAutoInstall : true
     * isIgnorable : true
     * versionCode : 2
     * versionName : 2.0
     * updateContent : 无忧服务系统客户端APP，版本：2.0；修复了URL错误
     * url : 地址
     * md5 : 7ba2af28eefa851b8ef36ee73d3e476d
     * size : 3721
     */

    private boolean hasUpdate;
    private boolean isSilent;
    private boolean isForce;
    private boolean isAutoInstall;
    private boolean isIgnorable;
    private String versionCode;
    private String versionName;
    private String updateContent;
    private String url;
    private String md5;
    private String size;

    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public void setSilent(boolean silent) {
        isSilent = silent;
    }

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean force) {
        isForce = force;
    }

    public boolean isAutoInstall() {
        return isAutoInstall;
    }

    public void setAutoInstall(boolean autoInstall) {
        isAutoInstall = autoInstall;
    }

    public boolean isIgnorable() {
        return isIgnorable;
    }

    public void setIgnorable(boolean ignorable) {
        isIgnorable = ignorable;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getUpdateContent() {
        return updateContent;
    }

    public void setUpdateContent(String updateContent) {
        this.updateContent = updateContent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "UpdateInfo{" +
                "hasUpdate=" + hasUpdate +
                ", isSilent=" + isSilent +
                ", isForce=" + isForce +
                ", isAutoInstall=" + isAutoInstall +
                ", isIgnorable=" + isIgnorable +
                ", versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", updateContent='" + updateContent + '\'' +
                ", url='" + url + '\'' +
                ", md5='" + md5 + '\'' +
                ", size='" + size + '\'' +
                '}';
    }
}
