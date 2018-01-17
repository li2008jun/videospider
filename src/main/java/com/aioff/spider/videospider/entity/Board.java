package com.aioff.spider.videospider.entity;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 视频列表页实体类
 */
public class Board {
    /** url的MD5签名 */
    private String _id;

    /** 版块url */
    private String url;

    /** 爬虫获取数据频道 */
    private String fromChannelName;

    /**
     * 全静态为0，
     * 列表页动态为1，
     * 正文页动态为2，
     * 全动态为3
     */
    private int dynamic;

    /** 开启标识 */
    private int isStart;
    /** 类型 */
    private int type = 1;

    /** 视频分类Id */
    private String videoClassId;
    /** 视频分类名称 */
    private String videoClassName;

    /** 视频来源id */
    private String videoSourceId;
    /** 视频来源站点名称 */
    private String videoSourceName = "";

    /** 抓取间隔-分钟 */
    private int fetchInterval;

    /** 下次抓取时间戳 */
    private long nextFetchTime;

    /** 最小播放次数 */
    private int minLookCnt;

    /** 今日头条视频分类 */
    private String toutiao_category;

    /** 秒拍分类 */
    private String miaopai_cateid;

    /** 网易短视频列表页url */
    private String wangyi_url;

    /** 美拍列表页tid-最热 */
    private String meipai_tid;

    /** 0-不下载&上传视频 */
    private int needUpload;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFromChannelName() {
        return fromChannelName;
    }

    public void setFromChannelName(String fromChannelName) {
        this.fromChannelName = fromChannelName;
    }

    public int getDynamic() {
        return dynamic;
    }

    public void setDynamic(int dynamic) {
        this.dynamic = dynamic;
    }

    public int getIsStart() {
        return isStart;
    }

    public void setIsStart(int isStart) {
        this.isStart = isStart;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getVideoClassId() {
        return videoClassId;
    }

    public void setVideoClassId(String videoClassId) {
        this.videoClassId = videoClassId;
    }

    public String getVideoClassName() {
        return videoClassName;
    }

    public void setVideoClassName(String videoClassName) {
        this.videoClassName = videoClassName;
    }

    public String getVideoSourceId() {
        return videoSourceId;
    }

    public void setVideoSourceId(String videoSourceId) {
        this.videoSourceId = videoSourceId;
    }

    public String getVideoSourceName() {
        return videoSourceName;
    }

    public void setVideoSourceName(String videoSourceName) {
        this.videoSourceName = videoSourceName;
    }

    public int getFetchInterval() {
        return fetchInterval;
    }

    public void setFetchInterval(int fetchInterval) {
        this.fetchInterval = fetchInterval;
    }

    public long getNextFetchTime() {
        return nextFetchTime;
    }

    public void setNextFetchTime(long nextFetchTime) {
        this.nextFetchTime = nextFetchTime;
    }

    public int getMinLookCnt() {
        return minLookCnt;
    }

    public void setMinLookCnt(int minLookCnt) {
        this.minLookCnt = minLookCnt;
    }

    public String getToutiao_category() {
        return toutiao_category;
    }

    public void setToutiao_category(String toutiao_category) {
        this.toutiao_category = toutiao_category;
    }

    public String getMiaopai_cateid() {
        return miaopai_cateid;
    }

    public void setMiaopai_cateid(String miaopai_cateid) {
        this.miaopai_cateid = miaopai_cateid;
    }

    public String getWangyi_url() {
        return wangyi_url;
    }

    public void setWangyi_url(String wangyi_url) {
        this.wangyi_url = wangyi_url;
    }

    public String getMeipai_tid() {
        return meipai_tid;
    }

    public void setMeipai_tid(String meipai_tid) {
        this.meipai_tid = meipai_tid;
    }

    public int getNeedUpload() {
        return needUpload;
    }

    public void setNeedUpload(int needUpload) {
        this.needUpload = needUpload;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

