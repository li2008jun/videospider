package com.aioff.spider.videospider.entity;

import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 视频信息表实体对象
 * Created by zhu lei on 2015/05/19
 */
public class VideoInfo {

    /** sourceUrl的MD5签名 */
    private String _id;

    /** 所属列表页模板信息 */
    private Board board;
    /** 正文页模板信息 */
    private Template template;

    /** 作者信息 */
    private AuthorInfo authorInfo;

    /** 标题 */
    private String title;
    /** 视频时长（秒）*/
    private long duration = 0L;
    /** 视频的封面图片 */
    private String cover;
    /** 视频的封面图片-本地图片路径 */
    private String cover_local;
    /** 视频的封面图片-上传OSS地址 */
    private String cover_oss;
    /** 内容 */
    private String content;
    /** 视频发布时间 */
    private String publishTime;

    /** 爬虫获取详情页Url */
    private String sourceUrl;
    /** 视频真实地址 */
    private String videoUrl;
    /** 视频下载的本地路径 */
    private String video_local;

    /** 抓取时间 */
    private String fetchTime;
    /** 抓取时间-时间戳 */
    private Long sequence;

    /** 状态 0代表不可用，1代表正常。默认为0 */
    private Integer status = 0;

    /** 播放数 */
    private long playCnt = 0L;
    /** 点赞数 */
    private long praiseCnt = 0L;
    /** 踩数 */
    private long trampleCnt = 0L;
    /** 评论数 */
    private long commentCnt = 0L;

    /** 今日头条保留信息 */
    private String toutiao_video_id;
    private String toutiao_group_id;

    /** 腾讯保留信息 */
    private String qq_video_id;

    /** 视频大小(MB) */
    private double video_size;
    /** 是否下载视频到本地 */
    private boolean needDownload = false;
    
    private  Map<String,Integer> keywordMap = null;

    public Map<String, Integer> getKeywordMap() {
		return keywordMap;
	}

	public void setKeywordMap(Map<String, Integer> keywordMap) {
		this.keywordMap = keywordMap;
	}

	public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public AuthorInfo getAuthorInfo() {
        return authorInfo;
    }

    public void setAuthorInfo(AuthorInfo authorInfo) {
        this.authorInfo = authorInfo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover_local() {
        return cover_local;
    }

    public void setCover_local(String cover_local) {
        this.cover_local = cover_local;
    }

    public String getCover_oss() {
        return cover_oss;
    }

    public void setCover_oss(String cover_oss) {
        this.cover_oss = cover_oss;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideo_local() {
        return video_local;
    }

    public void setVideo_local(String video_local) {
        this.video_local = video_local;
    }

    public String getFetchTime() {
        return fetchTime;
    }

    public void setFetchTime(String fetchTime) {
        this.fetchTime = fetchTime;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public long getPlayCnt() {
        return playCnt;
    }

    public void setPlayCnt(long playCnt) {
        this.playCnt = playCnt;
    }

    public long getPraiseCnt() {
        return praiseCnt;
    }

    public void setPraiseCnt(long praiseCnt) {
        this.praiseCnt = praiseCnt;
    }

    public long getTrampleCnt() {
        return trampleCnt;
    }

    public void setTrampleCnt(long trampleCnt) {
        this.trampleCnt = trampleCnt;
    }

    public long getCommentCnt() {
        return commentCnt;
    }

    public void setCommentCnt(long commentCnt) {
        this.commentCnt = commentCnt;
    }

    public String getToutiao_video_id() {
        return toutiao_video_id;
    }

    public void setToutiao_video_id(String toutiao_video_id) {
        this.toutiao_video_id = toutiao_video_id;
    }

    public String getToutiao_group_id() {
        return toutiao_group_id;
    }

    public void setToutiao_group_id(String toutiao_group_id) {
        this.toutiao_group_id = toutiao_group_id;
    }

    public double getVideo_size() {
        return video_size;
    }

    public void setVideo_size(double video_size) {
        this.video_size = video_size;
    }

    public boolean isNeedDownload() {
        return needDownload;
    }

    public void setNeedDownload(boolean needDownload) {
        this.needDownload = needDownload;
    }

    public String getQq_video_id() {
        return qq_video_id;
    }

    public void setQq_video_id(String qq_video_id) {
        this.qq_video_id = qq_video_id;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}