package com.tutu.compass;


/**
 * Created by tutu on 2017/7/16.
 */

public class UpdateBean {

    /**
     * Code : 1
     * Ext : Succeed
     * UpdateInfo : {"hasUpdate":"true","isSilent":"false","isForce":"true","isAutoInstall":"true","isIgnorable":"true","versionCode":"2","versionName":"2.0","updateContent":"无忧服务系统客户端APP，版本：2.0；修复了URL错误","url":"地址","md5":"7ba2af28eefa851b8ef36ee73d3e476d","size":"3721"}
     */

    private String Code;
    private String Ext;
    private UpdateInfo UpdateInfo;

    public String getCode() {
        return Code;
    }

    public void setCode(String Code) {
        this.Code = Code;
    }

    public String getExt() {
        return Ext;
    }

    public void setExt(String Ext) {
        this.Ext = Ext;
    }

    public UpdateInfo getUpdateInfo() {
        return UpdateInfo;
    }

    public void setUpdateInfo(UpdateInfo UpdateInfo) {
        this.UpdateInfo = UpdateInfo;
    }

    @Override
    public String toString() {
        return "UpdateBean{" +
                "Code='" + Code + '\'' +
                ", Ext='" + Ext + '\'' +
                ", UpdateInfo=" + UpdateInfo +
                '}';
    }
}
