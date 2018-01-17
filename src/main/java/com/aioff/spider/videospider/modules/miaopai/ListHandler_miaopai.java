package com.aioff.spider.videospider.modules.miaopai;

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
import java.util.List;

/**
 * 秒拍视频列表页接口处理
 * Created by zhengjunjun on 2017/5/22.
 */
public class ListHandler_miaopai {
    private static Logger logger = LoggerFactory.getLogger(ListHandler_miaopai.class);

    private static Downloader downloader = new HttpClientDownloader();

    /** 限制页数-秒拍 */
    private static int page_miaopai = SysConfig.getInstance().getIntegerVal("page_miaopai");

    public static ListHandler_miaopai getInstance() {
        return new ListHandler_miaopai();
    }

    /**
     * 下载，解析视频列表接口
     * @author zjj, 2017-05-22
     * @param board
     * @param pageNo - 初始为1
     * @return
     */
    public List<VideoInfo> getListInfo(Board board, int pageNo) {
        if (board == null || StringUtils.isBlank(board.getMiaopai_cateid())) {
            logger.error("miaopai_cateid不可为空！");
            return null;
        }
        List<VideoInfo> resultList = new ArrayList<VideoInfo>();

        // 合成接口请求url http://www.miaopai.com/miaopai/index_api?cateid=2002&per=100&page=1
        StringBuffer buffer = new StringBuffer();
        buffer.append("http://www.miaopai.com/miaopai/index_api?");
        buffer.append("cateid=" + board.getMiaopai_cateid());
        buffer.append("&per=100&page=" + pageNo);

        String url = buffer.toString();
        logger.info("秒拍列表页接口url = " + url);
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
            JSONArray jsonArray = jsonObject.getJSONArray("result");
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
                            JSONObject object_channel = object_result.getJSONObject("channel");

                            if (object_channel == null) {
                                logger.warn("json中缺少channel：" + url);
                                continue;
                            } else {
                                String scid = object_channel.getString("scid");
                                if (StringUtils.isNotBlank(scid)) {
                                    // 详情页url
                                    String sourceUrl = "http://www.miaopai.com/show/" + scid + ".htm";
                                    // 初步过滤url
                                    if (StringUtils.isBlank(sourceUrl) || !sourceUrl.startsWith("http")|| sourceUrl.contains("javascript")) {
                                        continue;
                                    }
                                    videoInfo.setSourceUrl(sourceUrl);
                                    videoInfo.set_id(Sign.getMD5(sourceUrl));

                                    JSONObject object_ext = object_channel.getJSONObject("ext");
                                    if (object_ext == null) {
                                        logger.warn("json中缺少ext：" + url);
                                        continue;
                                    } else {
                                        // 时长
                                        String duration_str = object_ext.getString("length");
                                        long duration = Long.parseLong(duration_str);
                                        videoInfo.setDuration(duration);

                                        // 标题
                                        String title = object_ext.getString("ft");
                                        videoInfo.setTitle(title);

                                        // 正文
                                        String content = object_ext.getString("_t");
                                        videoInfo.setContent(content);

                                        // 作者信息
                                        JSONObject object_owner = object_ext.getJSONObject("owner");
                                        if (object_owner == null) {
                                            logger.warn("json中缺少owner：" + url);
                                            continue;
                                        } else {
                                            // 作者ID
                                            String authorId = object_owner.getString("loginName");
                                            videoInfo.getAuthorInfo().setAuthorId(authorId);
                                            if (StringUtils.isNotBlank(authorId)) {
                                                String authorUrl = "http://www.miaopai.com/u/" + authorId;
                                                videoInfo.getAuthorInfo().setAuthorUrl(authorUrl);
                                            }

                                            // 作者名
                                            String authorName = object_owner.getString("nick");
                                            videoInfo.getAuthorInfo().setAuthorName(authorName);

                                            // 作者头像
                                            String authorImage = object_owner.getString("icon");
                                            videoInfo.getAuthorInfo().setAuthorImage(authorImage);
                                        }
                                    }

                                    // 发布时间
                                    JSONObject object_ext2 = object_channel.getJSONObject("ext2");
                                    if (object_ext2 != null) {
                                        try {
                                            long publishSequence = object_ext2.getLong("createTime");
                                            String publishTime = TimeUtil.sequenceToDate(String.valueOf(publishSequence));
                                            videoInfo.setPublishTime(publishTime);
                                        } catch (Exception e) {
                                            System.out.println(e);
                                        }
                                    }

                                    // 视频首图
                                    JSONObject object_pic = object_channel.getJSONObject("pic");
                                    if (object_pic == null) {
                                        logger.warn("json中缺少pic：" + url);
                                        continue;
                                    } else {
                                        String cover = object_pic.getString("base");
                                        if (StringUtils.isBlank(cover)) {
                                            logger.warn("json中缺少pic-base：" + url);
                                            continue;
                                        } else {
                                            videoInfo.setCover(cover);
                                        }
                                    }

                                    // 视频真实地址
                                    JSONObject object_stream = object_channel.getJSONObject("stream");
                                    if (object_stream == null) {
                                        logger.warn("json中缺少stream：" + url);
                                        continue;
                                    } else {
                                        //"base":"http://gslb.miaopai.com/stream/EmyRQpoNgGHWMsRhXSKtJk1u3kQfuwap.mp4?vend=miaopai&"
                                        String videoUrl = object_stream.getString("base");
                                        if (StringUtils.isBlank(videoUrl)) {
                                            logger.warn("json中缺少stream-base/sign：" + url);
                                            continue;
                                        } else {
                                            videoInfo.setVideoUrl(videoUrl);
                                        }
                                    }

                                    // 观看数等
                                    JSONObject object_stat = object_channel.getJSONObject("stat");
                                    if (object_stat == null) {
                                        logger.warn("json中缺少stat：" + url);
                                    } else {
                                        try {
                                            String playCnt_str = object_stat.getString("vcnt");
                                            long playCnt = JsoupUtils.getNum(playCnt_str);
                                            videoInfo.setPlayCnt(playCnt);

                                            String commentCnt_str = object_stat.getString("ccnt");
                                            long commentCnt = JsoupUtils.getNum(commentCnt_str);
                                            videoInfo.setCommentCnt(commentCnt);

                                            String praiseCnt_str = object_stat.getString("lcnt");
                                            long praiseCnt = JsoupUtils.getNum(praiseCnt_str);
                                            videoInfo.setPraiseCnt(praiseCnt);
                                        } catch (Exception e) {
                                            System.out.println(e);
                                        }
                                    }
                                }
                            }

                            resultList.add(videoInfo);
                        }
                    } catch (Exception e) {
                        logger.error("秒拍解析单条数据异常:" + url, e);
                    }
                }
                logger.info("*** 秒拍-本页数据量：{}, pageNo:{}, url:{}", jsonArray.size(), pageNo, url);

                // 翻页
                pageNo ++;
                if (pageNo <= page_miaopai) {
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
        board.setMiaopai_cateid("2002");
        List<VideoInfo> list = getInstance().getListInfo(board, 1);

        /*for (VideoInfo videoInfo : list) {
            System.out.println(videoInfo.getTitle() + ", " + videoInfo.getDuration() + "秒, " + ", " + videoInfo.getAuthorInfo().getAuthorName()
                    + ", " + videoInfo.getSourceUrl() + ", " + videoInfo.getPublishTime()
                    + ", " + videoInfo.getCover());
        }

        for (VideoInfo videoInfo : list) {
            System.out.println(JSON.toJSONString(videoInfo));
        }
*/
        System.out.println(list.size());
    }


}
