package com.aioff.spider.videospider.modules.toutiao;

import com.aioff.spider.downloader.Downloader;
import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import com.aioff.spider.videospider.entity.VideoInfo;
import com.aioff.spider.videospider.util.Base64Util;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.zip.CRC32;

/**
 * 今日头条视频视频详情接口处理
 * Created by zjj on 2017/05/22.
 */
public class VideoHandler_toutiao {
    private static Logger logger = LoggerFactory.getLogger(VideoHandler_toutiao.class);

    private static Downloader downloader = new HttpClientDownloader();

    public static VideoHandler_toutiao getInstance() {
        return new VideoHandler_toutiao();
    }

    private String getUrl(String video_id) {
        if (StringUtils.isBlank(video_id)) {
            logger.error("toutiao_video_id不可为空！");
            return null;
        }

        String url = null;
        // 生成参数r
        String r_init = String.valueOf(new Random().nextDouble());
        String r = r_init.substring(2);

        // 生成参数s 3344301371
        // crc32校验
        String href = "/video/urls/v/1/toutiao/mp4/" + video_id + "?r=" + r;
        CRC32 crc32 = new CRC32();
        crc32.update(href.getBytes());
        long s_init = crc32.getValue();
        // 移位
        long val = s_init >> 0;
        if (val < 0) {
            val = (val + 0x100000000L) >> 0;
        }
        String s = String.valueOf(val);

        // 合成接口url
        url = "http://ib.365yg.com/video/urls/v/1/toutiao/mp4/" + video_id
                + "?r=" + r + "&s=" + s;
        /*String url_1 = "http://i.snssdk.com/video/urls/v/1/toutiao/mp4/" + video_id
                + "?r=" + r + "&s=" + s + "&callback=tt_playerzfndr";*/
        return url;
    }

    /**
     * 获取视频详情接口信息
     * @author zjj, 2017-05-22
     * @param videoInfo
     */
    public void getVideoInfo(VideoInfo videoInfo) {
        if (videoInfo == null || StringUtils.isBlank(videoInfo.getToutiao_video_id())) {
            logger.error("toutiao_video_id不可为空！");
            return;
        }

        String url = getUrl(videoInfo.getToutiao_video_id());
        logger.info("今日头条视频地址接口url = " + url);
        if (StringUtils.isBlank(url)) {
            logger.error("视频详情接口url获取失败！" + url);
            return;
        }

        // 下载源码
        Page page = null;
        try {
            page = downloader.download(new Request(url));
        } catch (Exception e) {
            logger.error("下载异常：" + url, e);
            return;
        }

        String src = null;
        if (page == null || StringUtils.isBlank(page.getRawText())) {
            logger.error("源码下载失败：" + url);
            return;
        }
        src = page.getRawText();

        // 解析返回json
        try {
            JSONObject jsonObject_init = JSON.parseObject(src);
            JSONObject jsonObject = jsonObject_init.getJSONObject("data");
            if (jsonObject == null) {
                return;
            }

            // 时长
            if (videoInfo.getDuration() <= 0) {
                String duration_str = jsonObject.getString("video_duration");
                try {
                    if (StringUtils.isNotBlank(duration_str)) {
                        double temp = Double.parseDouble(duration_str);
                        long duration = (long)Math.floor(temp);
                        videoInfo.setDuration(duration);
                    }
                } catch (Exception e1) {
                    System.out.println(e1);
                }
            }

            // 视频真实地址
            JSONObject jsonObject_list = jsonObject.getJSONObject("video_list");
            if (jsonObject_list != null) {
                JSONObject jsonObject_2 = jsonObject_list.getJSONObject("video_2");
                if (jsonObject_2 != null) {
                    String main_url = jsonObject_2.getString("main_url");
                    if (StringUtils.isNotBlank(main_url)) {
                        String videoUrl = Base64Util.decodeBase64(main_url);
                        logger.info("videoUrl = " + videoUrl + ", sourceUrl = " + videoInfo.getSourceUrl());
                        videoInfo.setVideoUrl(videoUrl);
                    }
                }

                if (StringUtils.isBlank(videoInfo.getVideoUrl())) {
                    JSONObject jsonObject_1 = jsonObject_list.getJSONObject("video_1");
                    if (jsonObject_1 != null) {
                        String main_url = jsonObject_1.getString("main_url");
                        if (StringUtils.isNotBlank(main_url)) {
                            String videoUrl = Base64Util.decodeBase64(main_url);
                            logger.info("videoUrl = " + videoUrl + ", sourceUrl = " + videoInfo.getSourceUrl());
                            videoInfo.setVideoUrl(videoUrl);
                        }
                    }
                }

                if (StringUtils.isBlank(videoInfo.getVideoUrl())) {
                    JSONObject jsonObject_3 = jsonObject_list.getJSONObject("video_3");
                    if (jsonObject_3 != null) {
                        String main_url = jsonObject_3.getString("main_url");
                        if (StringUtils.isNotBlank(main_url)) {
                            String videoUrl = Base64Util.decodeBase64(main_url);
                            logger.info("videoUrl = " + videoUrl + ", sourceUrl = " + videoInfo.getSourceUrl());
                            videoInfo.setVideoUrl(videoUrl);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析json异常：" + url, e);
        }

    }

    /**
     * 测试用
     * @param args
     */
    public static void main(String[] args) {
        String video_id = "2166c378b63e4b499d8874a17f5d0932";
        //"b28b5aeb4d6b4cc287c848a4b7094911";
        System.out.println("url=" + getInstance().getUrl(video_id));

        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setToutiao_video_id(video_id);
        getInstance().getVideoInfo(videoInfo);

        System.out.println(videoInfo.getVideoUrl());
    }


}
