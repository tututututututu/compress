package com.tutu.compass;


/**
 * app版本信息实体类
 * Created by tutu on 17/3/6.
 */

public class VersionInfoBean{
    private String url;

    private boolean force;

    private String updateMsg;

    private boolean needUpdate;

    private String serverVersion;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getUpdateMsg() {
        return updateMsg;
    }

    public void setUpdateMsg(String updateMsg) {
        this.updateMsg = updateMsg;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Override
    public String toString() {
        return "VersionInfoBean{" +
                "url='" + url + '\'' +
                ", force=" + force +
                ", updateMsg='" + updateMsg + '\'' +
                ", needUpdate=" + needUpdate +
                ", serverVersion='" + serverVersion + '\'' +
                '}';
    }
}
