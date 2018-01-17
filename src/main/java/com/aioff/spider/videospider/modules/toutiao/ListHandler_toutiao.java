package com.aioff.spider.videospider.modules.toutiao;

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
import com.aioff.spider.videospider.util.UrlUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 今日头条视频列表页接口处理
 * Created by zhengjunjun on 2017/5/21.
 */
public class ListHandler_toutiao {
    private static Logger logger = LoggerFactory.getLogger(ListHandler_toutiao.class);

    /** 限制页数-今日头条 */
    private static int page_toutiao = SysConfig.getInstance().getIntegerVal("page_toutiao");

    private static Downloader downloader = new HttpClientDownloader();

    public static ListHandler_toutiao getInstance() {
        return new ListHandler_toutiao();
    }

    /**
     * 生成as、cp参数
     * @auhtor zjj, 2017-05-21
     * @return 如"as=A11519F20118713&cp=592168A72133BE1"
     */
    private String getAS_CP() {
        String as = null;
        String cp = null;

        long t = (long)Math.floor((new Date()).getTime() / 1000);
        String e = Long.toString(t, 16).toUpperCase();
        String i =  DigestUtils.md5Hex(Long.toString(t)).toUpperCase();

        if (e.length() != 8) {
            as = "479BB4B7254C150";
            cp =  "7E0AC8874BB0985";
            return "as=" + as + "&cp=" + cp;
        }

        String n = i.substring(0, 5);
        String a = i.substring(i.length() - 5, i.length());

        String s = "";
        for (int o = 0; o < 5; o++) {
            String a1 = String.valueOf(n.charAt(o));
            String a2 = String.valueOf(e.charAt(o));
            s += a1 + a2;
        }

        String r = "";
        for (int c = 0; c < 5; c++) {
            String a1 = String.valueOf(e.charAt(c + 3));
            String a2 = String.valueOf(a.charAt(c));
            r += a1 + a2;
        }

        as = "A1" + s + e.substring(e.length() - 3, e.length());
        cp = e.substring(0, 3) + r + "E1";

        return "as=" + as + "&cp=" + cp;
    }

    /**
     * 下载，解析视频列表接口
     * @author zjj, 2017-05-22
     * @param board
     * @param max_behot_time - 初始为0
     * @param pageNo - 初始为1
     * @return
     */
    public List<VideoInfo> getListInfo(Board board, long max_behot_time, int pageNo) {
        if (board == null || StringUtils.isBlank(board.getToutiao_category())) {
            logger.error("toutiao_category不可为空！");
            return null;
        }
        List<VideoInfo> resultList = new ArrayList<VideoInfo>();

        // 合成接口请求url
        StringBuffer buffer = new StringBuffer();
        buffer.append("http://www.toutiao.com/api/pc/feed/?category=" + board.getToutiao_category());
        buffer.append("&utm_source=toutiao&widen=1");
        buffer.append("&max_behot_time=" + max_behot_time);
        buffer.append("&max_behot_time_tmp=0");
        buffer.append("&tadrequire=true");
        buffer.append("&" + getAS_CP());

        String url = buffer.toString();
        logger.info("今日头条列表页接口url = " + url);
        // 下载源码
        Request request = new Request(url);
        request.addHead("charset", "UTF-8");
        Page page = null;
        try {
            page = downloader.download(new Request(url));
        } catch (Exception e) {
            logger.error("下载异常：" + url, e);
            return null;
        }

        String src = null;
        if (page == null || StringUtils.isBlank(page.getRawText())) {
            logger.error("源码下载失败：" + url);
            return null;
        }
        src = page.getRawText();

        // 解析返回json
        try {
            JSONObject jsonObject = JSON.parseObject(src);
            // 本页数据
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    try {
                        JSONObject object = (JSONObject)jsonArray.get(i);
                        if (object != null) {
                            String title = object.getString("title");
                            String cover = object.getString("image_url");

                            String video_id = object.getString("video_id");
                            String group_id = object.getString("group_id");
                            String sourceUrl = object.getString("source_url");
                            sourceUrl = UrlUtil.canonicalizeUrl(sourceUrl, "http://www.toutiao.com/");
                            // 初步过滤url
                            if (StringUtils.isBlank(sourceUrl) || !sourceUrl.startsWith("http") || sourceUrl.contains("javascript")) {
                                continue;
                            }

                            String duration_str = object.getString("video_duration_str");
                            long duration = JsoupUtils.getDuration(duration_str);

                            String behot_time = object.getString("behot_time");
                            String publishTime = TimeUtil.sequenceToDate(behot_time + "000");
                            String playCnt = object.getString("video_play_count");
                            String commentCnt = object.getString("comments_count");

                            String authorName = object.getString("source");
                            String authorImage = object.getString("media_avatar_url");
                            String authorUrl = object.getString("media_url");
                            authorUrl = UrlUtil.canonicalizeUrl(authorUrl, "http://www.toutiao.com/");
                            // /c/user/60081791844/
                            String authorId = null;
                            if (StringUtils.isNotBlank(authorUrl)) {
                                int start = authorUrl.indexOf("user/");
                                if (authorUrl.endsWith("/")) {
                                    authorId = authorUrl.substring(start + 5, authorUrl.length() - 1);
                                } else {
                                    authorId = authorUrl.substring(start + 5);
                                }
                            }

                            VideoInfo videoInfo = new VideoInfo();
                            videoInfo.setBoard(board);
                            AuthorInfo authorInfo = new AuthorInfo();
                            videoInfo.setAuthorInfo(authorInfo);
                            if (StringUtils.isNotBlank(sourceUrl) && StringUtils.isNotBlank(video_id)) {
                                videoInfo.setSourceUrl(sourceUrl);
                                videoInfo.set_id(Sign.getMD5(sourceUrl));
                                videoInfo.setTitle(title);
                                videoInfo.setCover(UrlUtil.transformUrl(cover));
                                videoInfo.setPublishTime(publishTime);
                                videoInfo.setDuration(duration);

                                videoInfo.getAuthorInfo().setAuthorId(authorId);
                                videoInfo.getAuthorInfo().setAuthorName(authorName);
                                videoInfo.getAuthorInfo().setAuthorImage(authorImage);
                                videoInfo.getAuthorInfo().setAuthorUrl(authorUrl);

                                videoInfo.setToutiao_group_id(group_id);
                                videoInfo.setToutiao_video_id(video_id);

                                try {
                                    if (StringUtils.isNotBlank(playCnt)) {
                                        videoInfo.setPlayCnt(Long.parseLong(playCnt));
                                    }
                                    if (StringUtils.isNotBlank(commentCnt)) {
                                        videoInfo.setCommentCnt(Long.parseLong(commentCnt));
                                    }
                                } catch (Exception e1) {
                                    logger.error(e1.getMessage());
                                }

                                resultList.add(videoInfo);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("头条解析单条数据异常:" + url, e);
                    }
                }
                logger.info("*** 今日头条-本页数据量：{}, pageNo:{}, url:{}", jsonArray.size(), pageNo, url);

                // 下一页参数
                JSONObject jsonObject_next = jsonObject.getJSONObject("next");
                if (jsonObject_next != null && jsonObject_next.containsKey("max_behot_time")) {
                    long max_time = jsonObject_next.getLong("max_behot_time");
                    pageNo ++;
                    if (pageNo <= page_toutiao) {
                        List<VideoInfo> temp = getListInfo(board, max_time, pageNo);
                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(temp)) {
                            resultList.addAll(temp);
                        }
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
        String param = getInstance().getAS_CP();

        Board board = new Board();
        board.setToutiao_category("%E4%B8%AD%E5%9B%BD%E6%96%B0%E5%94%B1%E5%B0%86");
        List<VideoInfo> list = getInstance().getListInfo(board, 0 , 1);

        for (VideoInfo videoInfo : list) {
            System.out.println(videoInfo.getTitle() + ", " + videoInfo.getDuration() + "秒, " + ", " + videoInfo.getAuthorInfo().getAuthorName()
                    + ", " + videoInfo.getSourceUrl() + ", " + videoInfo.getPublishTime()
                    + ", " + videoInfo.getCover());
        }

        for (VideoInfo videoInfo : list) {
            System.out.println(JSON.toJSONString(videoInfo));
        }

        System.out.println(param + ", size = " + list.size());
    }

}
