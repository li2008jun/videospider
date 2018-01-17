package com.aioff.spider.videospider.modules.wangyi;

import com.aioff.spider.downloader.Downloader;
import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import com.aioff.spider.util.Sign;
import com.aioff.spider.util.TimeUtil;
import com.aioff.spider.videospider.db.mongo.VideoTemplateMongoDAO;
import com.aioff.spider.videospider.entity.AuthorInfo;
import com.aioff.spider.videospider.entity.Board;
import com.aioff.spider.videospider.entity.Template;
import com.aioff.spider.videospider.entity.VideoInfo;
import com.aioff.spider.videospider.util.JsoupUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 网易视频列表页接口处理
 * Created by zhengjunjun on 2017/5/23.
 */
public class ListHandler_wangyi {
    private static Logger logger = LoggerFactory.getLogger(ListHandler_wangyi.class);

    private static Downloader downloader = new HttpClientDownloader();

    private static VideoTemplateMongoDAO videoTemplateMongoDAOInstance = VideoTemplateMongoDAO.getInstance();

    public static ListHandler_wangyi getInstance() {
        return new ListHandler_wangyi();
    }

    /**
     * 下载，解析视频列表接口
     * @author zjj, 2017-05-22
     * @param board
     * @return
     */
    public List<VideoInfo> getListInfo(Board board) {
        if (board == null || StringUtils.isBlank(board.getWangyi_url())) {
            logger.error("wangyi_url不可为空！");
            return null;
        }

        // 加载模板
        Template template = videoTemplateMongoDAOInstance.find(board.getVideoSourceId());
        if (template == null) {
            logger.info("模板未找到！url = " + board.getUrl());
        }

        List<VideoInfo> resultList = new ArrayList<VideoInfo>();

        String url = board.getWangyi_url();
        logger.info("网易列表页接口url = " + url);
        // 下载源码
        Page page = null;
        try {
            page = downloader.download(new Request(url));
        } catch (Exception e) {
            logger.error("下载异常：" + url, e);
            return null;
        }

        String src = null;
        if (page == null || StringUtils.isBlank(page.getRawText())
                || !page.getRawText().contains("callback_video(") || !page.getRawText().contains(")")) {
            logger.error("源码下载失败：" + url);
            return null;
        }
        src = page.getRawText();
        src = src.substring("callback_video(".length(), src.length() - 1);

        // 解析返回json
        try {
            JSONObject jsonObject = JSON.parseObject(src);
            JSONArray jsonArray = jsonObject.getJSONArray("list");
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    try {
                        VideoInfo videoInfo = new VideoInfo();
                        videoInfo.setBoard(board);
                        videoInfo.setTemplate(template);
                        AuthorInfo authorInfo = new AuthorInfo();
                        videoInfo.setAuthorInfo(authorInfo);

                        JSONObject object = (JSONObject)jsonArray.get(i);

                        if (object == null) {
                            continue;
                        } else {
                            // 详情页url
                            String sourceUrl = object.getString("url");
                            // 初步过滤url
                            if (StringUtils.isBlank(sourceUrl) || sourceUrl.contains("javascript")) {
                                continue;
                            }
                            videoInfo.setSourceUrl(sourceUrl);
                            videoInfo.set_id(Sign.getMD5(sourceUrl));

                            // 标题
                            String title = object.getString("title");
                            videoInfo.setTitle(title);

                            // 视频首图
                            String cover = object.getString("img");
                            if (StringUtils.isBlank(cover)) {
                                logger.warn("json中缺少img：" + url);
                                continue;
                            } else {
                                videoInfo.setCover(cover);
                            }

                            // 时长
                            try {
                                String duration_str = object.getString("playlength");
                                long duration = Long.parseLong(duration_str);
                                videoInfo.setDuration(duration);
                            } catch (Exception e) {
                                System.out.println(e);
                            }

                            // 作者名
                            String authorName = object.getString("sname");
                            videoInfo.getAuthorInfo().setAuthorName(authorName);

                            // 观看数等
                            JSONObject object_vote = object.getJSONObject("vote");
                            if (object_vote == null) {
                                logger.warn("json中缺少vote：" + url);
                            } else {
                                JSONObject object_info = object_vote.getJSONObject("info");
                                if (object_info == null) {
                                    logger.warn("json中缺少vote.info：" + url);
                                } else {
                                    try {
                                        String playCnt_str = object_info.getString("hits");
                                        long playCnt = JsoupUtils.getNum(playCnt_str);
                                        videoInfo.setPlayCnt(playCnt);

                                        String praiseCnt_str = object_info.getString("supportcount");
                                        long praiseCnt = JsoupUtils.getNum(praiseCnt_str);
                                        videoInfo.setPraiseCnt(praiseCnt);
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                }
                            }

                        }

                        resultList.add(videoInfo);
                    } catch (Exception e) {
                        logger.error("网易解析单条数据异常：" + url, e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析json异常：" + url, e);
        }

        return resultList;
    }

    /**
     * 测试用
     * @param args
     */
    public static void main(String[] args) {
        Board board = new Board();
        board.setWangyi_url("http://v.163.com/special/video_tuijian/?callback=callback_video");
        List<VideoInfo> list = getInstance().getListInfo(board);

        for (VideoInfo videoInfo : list) {
            System.out.println(videoInfo.getTitle() + ", " + videoInfo.getDuration() + "秒, " + ", " + videoInfo.getAuthorInfo().getAuthorName()
                    + ", " + videoInfo.getSourceUrl() + ", " + videoInfo.getPublishTime()
                    + ", " + videoInfo.getCover());
        }

        for (VideoInfo videoInfo : list) {
            System.out.println(JSON.toJSONString(videoInfo));
        }

        System.out.println("size = " + list.size());
    }
}
