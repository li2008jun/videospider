package com.aioff.spider.videospider.modules.lishipin;

import com.aioff.spider.downloader.Downloader;
import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import com.aioff.spider.videospider.entity.VideoInfo;
import com.aioff.spider.videospider.util.HttpUtil;
import com.aioff.spider.videospider.util.JsoupUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 网易短视频详情页处理
 * Created by zhengjunjun on 2017/5/23.
 */
public class VideoHandler_lishipin {
    private static Logger logger = LoggerFactory.getLogger(VideoHandler_lishipin.class);

    private static Downloader downloader = new HttpClientDownloader();

    public static VideoHandler_lishipin getInstance() {
        return new VideoHandler_lishipin();
    }

    /**
     * 获取视频详情接口信息
     * @author zjj, 2017-05-23
     * @param videoInfo
     */
    public void getVideoInfo(VideoInfo videoInfo) {
        if (videoInfo == null || StringUtils.isBlank(videoInfo.getSourceUrl())) {
            logger.error("sourceUrl不可为空！");
            return;
        }

        String url = videoInfo.getSourceUrl();

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
//        String src = HttpUtil.getHTML(url);

        // 解析视频真实地址
        try {
            Document doc = Jsoup.parse(src);
            logger.info("src:" + src);
            String script = JsoupUtils.extractText(doc, "div.prism-player", 3);
            if (StringUtils.isNotBlank(script)) {
            	  logger.info("script:" + script);
                String videoPart = JsoupUtils.regexText(script, ".*<video(.*?)</video>.*");
                if (StringUtils.isBlank(videoPart)) {
                    logger.warn("网易视频真实地址解析失败：" + url);
                } else {
                    Document doc_video = Jsoup.parse(videoPart);
                    String videoUrl = JsoupUtils.extractText(doc_video, "source src", 0);
                    videoInfo.setVideoUrl(videoUrl);
                }
            }
        } catch (Exception e) {
            logger.error("解析视频真实地址异常：" + url, e);
        }

    }

    /**
     * 测试用
     * @param args
     */
    public static void main(String[] args) {
        String url = "http://www.pearvideo.com/video_1148008";
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setSourceUrl(url);
        getInstance().getVideoInfo(videoInfo);

        System.out.println(videoInfo.getVideoUrl());
    }

}
