package com.aioff.spider.videospider.entity;

/**
 * 网站代码
 * Created by zjj on 2017/05/22.
 */
public enum SiteEnum {
    MEIPAI("meipai.com"), //美拍
    MIAOPAI("miaopai.com"), //秒拍
    WANGYI("163.com"), //网易短视频
    TOUTIAO("toutiao.com"); //今日头条

    private String value;

    private SiteEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
