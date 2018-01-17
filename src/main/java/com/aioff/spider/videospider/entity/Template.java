package com.aioff.spider.videospider.entity;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 视频正文页模板实体类
 * Created by zhu lei on 2016/05/05
 */
public class Template {

    /** id = host */
    private String _id;

    /** 网站名 */
    private String name;

    /** 单个视频div - 列表页 */
    private String listDiv;
    /** 正文页url - 列表页 */
    private String listSourceUrl;
    /** 视频真实url - 列表页 */
    private String listVideoUrl;
    /** 标题 - 列表页 */
    private String listTitle;
    /** 封面图片 - 列表页 */
    private String listCover;
    /** 发布时间 - 列表页 */
    private String listTime;
    /** 播放数 - 列表页 */
    private String listPlayCnt;
    /** 点赞数 - 列表页 */
    private String listPraiseCnt;
    /** 评论数 - 列表页 */
    private String listCommentCnt;

    /** 标题 - 正文页 */
    private String detailTitle;
    /** 简介 - 正文页 */
    private String detailContent;
    /** 发布时间 - 正文页 */
    private String detailTime;
    /** 时长(秒) - 正文页 */
    private String detailDuration;
    /** 视频真实url - 正文页 */
    private String detailVideoUrl;

    /** 播放数 - 正文页 */
    private String detailPlayCnt;
    /** 点赞数 - 正文页 */
    private String detailPraiseCnt;
    /** 踩数 - 正文页 */
    private String detailTrampleCnt;
    /** 评论数- 正文页 */
    private String detailCommentCnt;

    /** 视频获取真实url方式：flvcd  shokdown */
    private String videoUrlUtil;

    /** 作者名称 */
    private String authorName;
    /** 作者名id */
    private String authorId;
    /** 作者头像 */
    private String authorImage;
    /** 作者个人页面url */
    private String authorUrl;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListDiv() {
        return listDiv;
    }

    public void setListDiv(String listDiv) {
        this.listDiv = listDiv;
    }

    public String getListSourceUrl() {
        return listSourceUrl;
    }

    public void setListSourceUrl(String listSourceUrl) {
        this.listSourceUrl = listSourceUrl;
    }

    public String getListVideoUrl() {
        return listVideoUrl;
    }

    public void setListVideoUrl(String listVideoUrl) {
        this.listVideoUrl = listVideoUrl;
    }

    public String getListTitle() {
        return listTitle;
    }

    public void setListTitle(String listTitle) {
        this.listTitle = listTitle;
    }

    public String getListCover() {
        return listCover;
    }

    public void setListCover(String listCover) {
        this.listCover = listCover;
    }

    public String getListTime() {
        return listTime;
    }

    public void setListTime(String listTime) {
        this.listTime = listTime;
    }

    public String getListPlayCnt() {
        return listPlayCnt;
    }

    public void setListPlayCnt(String listPlayCnt) {
        this.listPlayCnt = listPlayCnt;
    }

    public String getListPraiseCnt() {
        return listPraiseCnt;
    }

    public void setListPraiseCnt(String listPraiseCnt) {
        this.listPraiseCnt = listPraiseCnt;
    }

    public String getListCommentCnt() {
        return listCommentCnt;
    }

    public void setListCommentCnt(String listCommentCnt) {
        this.listCommentCnt = listCommentCnt;
    }

    public String getDetailTitle() {
        return detailTitle;
    }

    public void setDetailTitle(String detailTitle) {
        this.detailTitle = detailTitle;
    }

    public String getDetailContent() {
        return detailContent;
    }

    public void setDetailContent(String detailContent) {
        this.detailContent = detailContent;
    }

    public String getDetailTime() {
        return detailTime;
    }

    public void setDetailTime(String detailTime) {
        this.detailTime = detailTime;
    }

    public String getDetailDuration() {
        return detailDuration;
    }

    public void setDetailDuration(String detailDuration) {
        this.detailDuration = detailDuration;
    }

    public String getDetailVideoUrl() {
        return detailVideoUrl;
    }

    public void setDetailVideoUrl(String detailVideoUrl) {
        this.detailVideoUrl = detailVideoUrl;
    }

    public String getDetailPlayCnt() {
        return detailPlayCnt;
    }

    public void setDetailPlayCnt(String detailPlayCnt) {
        this.detailPlayCnt = detailPlayCnt;
    }

    public String getDetailPraiseCnt() {
        return detailPraiseCnt;
    }

    public void setDetailPraiseCnt(String detailPraiseCnt) {
        this.detailPraiseCnt = detailPraiseCnt;
    }

    public String getDetailTrampleCnt() {
        return detailTrampleCnt;
    }

    public void setDetailTrampleCnt(String detailTrampleCnt) {
        this.detailTrampleCnt = detailTrampleCnt;
    }

    public String getDetailCommentCnt() {
        return detailCommentCnt;
    }

    public void setDetailCommentCnt(String detailCommentCnt) {
        this.detailCommentCnt = detailCommentCnt;
    }

    public String getVideoUrlUtil() {
        return videoUrlUtil;
    }

    public void setVideoUrlUtil(String videoUrlUtil) {
        this.videoUrlUtil = videoUrlUtil;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorImage() {
        return authorImage;
    }

    public void setAuthorImage(String authorImage) {
        this.authorImage = authorImage;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
