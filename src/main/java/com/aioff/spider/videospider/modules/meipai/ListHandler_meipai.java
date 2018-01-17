package com.aioff.spider.videospider.modules.meipai;

import com.aioff.spider.downloader.Downloader;
import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import com.aioff.spider.util.Sign;
import com.aioff.spider.util.TimeUtil;
import com.aioff.spider.videospider.entity.AuthorInfo;
import com.aioff.spider.videospider.entity.Board;
import com.aioff.spider.videospider.entity.VideoInfo;
import com.aioff.spider.videospider.util.JsoupUtils;
import com.aioff.spider.videospider.util.SysConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 美拍视频列表页接口处理
 * Created by zhengjunjun on 2017/7/22.
 */
public class ListHandler_meipai {
    private static Logger logger = LoggerFactory.getLogger(ListHandler_meipai.class);

    private static Downloader downloader = new HttpClientDownloader();

    /** 限制页数-美拍 */
    private static int page_meipai = SysConfig.getInstance().getIntegerVal("page_meipai");

    public static ListHandler_meipai getInstance() {
        return new ListHandler_meipai();
    }

    /**
     * 下载，解析视频列表接口
     * @author zjj, 2017-07-22
     * @param board
     * @param pageNo - 初始为1
     * @return
     */
    public List<VideoInfo> getListInfo(Board board, int pageNo) {
        if (board == null || StringUtils.isBlank(board.getMeipai_tid())) {
            logger.error("meipai_tid不可为空！");
            return null;
        }
        List<VideoInfo> resultList = new ArrayList<VideoInfo>();

        // 合成接口请求url http://www.meipai.com/squares/new_timeline?tid=13&page=100&count=100
        StringBuffer buffer = new StringBuffer();
        buffer.append("http://www.meipai.com/");
        buffer.append(board.getMeipai_tid());
        buffer.append("&count=100&page=" + pageNo);

        String url = buffer.toString();
        logger.info("美拍列表页接口url = " + url);
        // 下载源码
        Page page = null;
        try {
            page = downloader.download(new Request(url));
        } catch (Exception e) {
            logger.error("下载异常：" + url, e);
            return null;
        }

        String src = null;
        if (page == null || StringUtils.isBlank(page.getRawText()) || page.getRawText().equals("null")) {
            logger.error("源码下载失败：" + url);
            return null;
        }
        src = page.getRawText();

        // 解析返回json
        try {
            JSONObject jsonObject = JSON.parseObject(src);
            JSONArray jsonArray = jsonObject.getJSONArray("medias");
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    try {
                        VideoInfo videoInfo = new VideoInfo();
                        videoInfo.setBoard(board);
                        AuthorInfo authorInfo = new AuthorInfo();
                        videoInfo.setAuthorInfo(authorInfo);

                        JSONObject object_result = (JSONObject)jsonArray.get(i);

                        if (object_result == null) {
                            continue;
                        } else {
                            String id = object_result.getString("id");

                            // 详情页url
                            String sourceUrl = object_result.getString("url");
                            // 初步过滤url
                            if (StringUtils.isBlank(sourceUrl) || !sourceUrl.startsWith("http") || sourceUrl.contains("javascript")) {
                                logger.warn("url不合法：{}, listUrl[{}]", sourceUrl, url);
                                continue;
                            }
                            videoInfo.setSourceUrl(sourceUrl);
                            videoInfo.set_id(Sign.getMD5(sourceUrl));

                            // 时长
                            String duration_str = object_result.getString("time");
                            long duration = Long.parseLong(duration_str);
                            videoInfo.setDuration(duration);

                            // 标题
                            String title = object_result.getString("caption_complete");
                            videoInfo.setTitle(title);

                            // 正文
                            String content = object_result.getString("caption_complete");
                            videoInfo.setContent(content);

                            // 发布时间
                            String publichStr = object_result.getString("created_at_origin");
                            if (StringUtils.isBlank(publichStr)) {
                                logger.warn("发布时间不合法：{}, listUrl[{}]", publichStr, url);
                                continue;
                            }
                            String publishTime = TimeUtil.sequenceToDate(publichStr + "000");
                            videoInfo.setPublishTime(publishTime);

                            // 视频首图
                            String cover = object_result.getString("cover_pic");
                            if (StringUtils.isBlank(cover)) {
                                logger.warn("json中缺少cover_pic：" + url);
                                continue;
                            } else {
                                videoInfo.setCover(cover);
                            }

                            // 作者信息
                            JSONObject object_user = object_result.getJSONObject("user");
                            if (object_user == null) {
                                logger.warn("json中缺少user：" + url);
                                continue;
                            } else {
                                // 作者ID
                                String authorId = object_user.getString("id");
                                videoInfo.getAuthorInfo().setAuthorId(authorId);

                                // 作者名
                                String authorName = object_user.getString("screen_name");
                                videoInfo.getAuthorInfo().setAuthorName(authorName);

                                // 作者头像
                                String authorImage = object_user.getString("avatar");
                                videoInfo.getAuthorInfo().setAuthorImage(authorImage);

                                // 作者url
                                String authorUrl = object_user.getString("url");
                                videoInfo.getAuthorInfo().setAuthorUrl(authorUrl);
                            }
                        }

                        // 点赞、评论数等
                        String commentCnt_str = object_result.getString("comments_count");
                        long commentCnt = JsoupUtils.getNum(commentCnt_str);
                        videoInfo.setCommentCnt(commentCnt);

                        String praiseCnt_str = object_result.getString("likes_count");
                        long praiseCnt = JsoupUtils.getNum(praiseCnt_str);
                        videoInfo.setPraiseCnt(praiseCnt);

                        // 视频真实地址
                        String videoUrl = object_result.getString("video");
                        if (StringUtils.isBlank(videoUrl)) {
                            logger.warn("json中缺少video：" + url);
                            continue;
                        } else {
                            videoInfo.setVideoUrl(videoUrl);
                        }

                        resultList.add(videoInfo);
                    } catch (Exception e) {
                        logger.error("美拍解析单条数据异常:" + url, e);
                    }
                }
                logger.info("*** 美拍-本页数据量：{}, pageNo:{}, url:{}", jsonArray.size(), pageNo, url);

                // 翻页
                pageNo ++;
                if (pageNo <= page_meipai) {
                    List<VideoInfo> temp = getListInfo(board, pageNo);
                    if (CollectionUtils.isNotEmpty(temp)) {
                        resultList.addAll(temp);
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
        board.setMeipai_tid("squares/new_timeline?tid=13");
        List<VideoInfo> list = getInstance().getListInfo(board, 1);
        Set<String> set = new HashSet<String>();
        for (VideoInfo videoInfo : list) {
            String url = videoInfo.getSourceUrl();
            set.add(url);
            System.out.println(JSON.toJSONString(videoInfo));
        }

        System.out.println(list.size() + ",去重后：" + set.size());
    }

}
