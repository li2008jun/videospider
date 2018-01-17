package com.aioff.spider.videospider.parse;

import com.aioff.spider.Constant;
import com.aioff.spider.util.ExcTime;
import com.aioff.spider.util.UrlUtils;
import com.aioff.spider.videospider.entity.VideoInfo;
import com.aioff.spider.videospider.util.JsoupUtils;
import com.sun.corba.se.impl.protocol.giopmsgheaders.CancelRequestMessage_1_0;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 抽取信息方法类
 * Created by zhengjunjun on 2017/5/17.
 */
public class ParseHandler {
    private static Logger logger = LoggerFactory.getLogger(ParseHandler.class);

    /**
     * 获取qq视频地址
     */
    public String getQQVideoUrl(String url) {
        String vid = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
        String videoUrl = "http://imgcache.qq.com/tencentvideo_v1/player/TencentPlayer.swf?vid=" + vid;
        return videoUrl;
    }

    /**
     * 抽取详情页url
     * @author zjj, 2017-05-17
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getListSourceUrl(Element element, String tags, VideoInfo videoInfo) {
        try {
            String sourceUrl = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(sourceUrl) || !sourceUrl.startsWith("http")) {
                return false;
            }

            videoInfo.setSourceUrl(sourceUrl);
            return true;
        } catch (Exception e) {
            logger.error("抽取正文页url异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取视频真实url
     * @author zjj, 2017-05-17
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getVideoUrl(Element element, String tags, VideoInfo videoInfo) {
        try {
            String videoUrl = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(videoUrl) || !videoUrl.startsWith("http")) {
                return false;
            }

            videoInfo.setVideoUrl(videoUrl);
            return true;
        } catch (Exception e) {
            logger.error("抽取视频真实url异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取标题
     * @author zjj, 2017-05-17
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getTitle(Element element, String tags, VideoInfo videoInfo) {
        try {
            String title = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isNotBlank(title)) {
                title = title.replaceAll("\\(\\d+\\)", "").trim();
                title = title.replaceFirst(" ", "");
            }

            if (StringUtils.isBlank(title) || title.length() < 2) {
                return false;
            }

            videoInfo.setTitle(title);
            return true;
        } catch (Exception e) {
            logger.error("抽取标题异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取视频首图
     * @author zjj, 2017-05-17
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getCover(Element element, String tags, VideoInfo videoInfo) {
        try {
            String imageUrl = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(imageUrl) || !imageUrl.startsWith("http")) {
                return false;
            }

            videoInfo.setCover(imageUrl);
            return true;
        } catch (Exception e) {
            logger.error("抽取视频首图异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取简介
     * @author zjj, 2017-05-17
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getContent(Element element, String tags, VideoInfo videoInfo) {
        try {
            String content = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(content)) {
                return false;
            }

            content = content.replace("[+展开简介]", "");
            content = content.replace("简介", "");
            videoInfo.setContent(content);
            return true;
        } catch (Exception e) {
            logger.error("抽取简介异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取发布时间
     * @author zjj, 2017-05-17
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getTime(Element element, String tags, VideoInfo videoInfo) {
        try {
            String time = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(time)) {
                return false;
            }

            // 初步判断时间存在于正文内容之
            Date date = ExcTime.getDate(time);
            if (date == null) {
                return false;
            }
            time = DateFormatUtils.format(date, Constant.DEFAULT_TIME);
            videoInfo.setPublishTime(time);
            return true;
        } catch (Exception e) {
            logger.error("抽取发布时间异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取时长-秒
     * @author zjj, 2017-05-17
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getDuration(Element element, String tags, VideoInfo videoInfo) {
        try {
            String duration_str = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(duration_str)) {
                return false;
            }

            long duration = JsoupUtils.getDuration(duration_str);
            videoInfo.setDuration(duration);
            return true;
        } catch (Exception e) {
            logger.error("抽取时长异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取播放量
     * @author zjj, 2017-05-18
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getPlayCnt(Element element, String tags, VideoInfo videoInfo) {
        try {
            String count = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(count)) {
                return false;
            }

            if (count.contains("观看")) {
                //去除秒拍和发布时间一起的观看信息，如（16:29   1.6万 观看）(401万观看)
                String[] strs = count.split("   ");
                if (strs.length >= 2) {
                    count = strs[1];
                }
            }

            if (StringUtils.isNotBlank(count)) {
                double count_init = JsoupUtils.extractDouble(count);
                long num = 0L;
                if (count.contains("万")) {
                    Double s = count_init * 10000;
                    num = new Double(s).longValue();
                } else {
                    num = new Double(count_init).longValue();
                }
                videoInfo.setPlayCnt(num);
            }
            return true;
        } catch (Exception e) {
            logger.error("抽取播放量异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取点赞数
     * @author zjj, 2017-05-18
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getPraiseCnt(Element element, String tags, VideoInfo videoInfo) {
        try {
            String count = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(count)) {
                return false;
            }

            if (StringUtils.isNotBlank(count)) {
                double count_init = JsoupUtils.extractDouble(count);
                long num = 0L;
                if (count.contains("万")) {
                    Double s = count_init * 10000;
                    num = new Double(s).longValue();
                } else {
                    num = new Double(count_init).longValue();
                }
                videoInfo.setPraiseCnt(num);
            }

            return true;
        } catch (Exception e) {
            logger.error("抽取点赞数异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取踩数
     * @author zjj, 2017-05-18
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getTrampleCnt(Element element, String tags, VideoInfo videoInfo) {
        try {
            String count = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(count)) {
                return false;
            }

            if (StringUtils.isNotBlank(count)) {
                double count_init = JsoupUtils.extractDouble(count);
                long num = 0L;
                if (count.contains("万")) {
                    Double s = count_init * 10000;
                    num = new Double(s).longValue();
                } else {
                    num = new Double(count_init).longValue();
                }
                videoInfo.setTrampleCnt(num);
            }
            return true;
        } catch (Exception e) {
            logger.error("抽取踩数异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取评论数
     * @author zjj, 2017-05-17
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getCommentCnt(Element element, String tags, VideoInfo videoInfo) {
        try {
            String count = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(count)) {
                return false;
            }

            /*if (count.contains("评论")) {
                //去除秒拍（评论 10）
                String[] strs = count.split(" ");
                count = strs[1];
            }*/

            if (StringUtils.isNotBlank(count)) {
                double count_init = JsoupUtils.extractDouble(count);
                long num = 0L;
                if (count.contains("万")) {
                    Double s = count_init * 10000;
                    num = new Double(s).longValue();
                } else {
                    num = new Double(count_init).longValue();
                }
                videoInfo.setCommentCnt(num);
            }
            return true;
        } catch (Exception e) {
            logger.error("抽取评论数异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取作者ID
     * @author zjj, 2017-05-18
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getAuthorId(Element element, String tags, VideoInfo videoInfo) {
        try {
            String authorId = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(authorId)) {
                return false;
            }

            videoInfo.getAuthorInfo().setAuthorId(authorId);
            return true;
        } catch (Exception e) {
            logger.error("抽取作者ID异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取作者名称
     * @author zjj, 2017-05-18
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getAuthorName(Element element, String tags, VideoInfo videoInfo) {
        try {
            String authorName = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(authorName)) {
                return false;
            }

            videoInfo.getAuthorInfo().setAuthorName(authorName);
            return true;
        } catch (Exception e) {
            logger.error("抽取作者名称异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取作者头像
     * @author zjj, 2017-05-18
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getAuthorImage(Element element, String tags, VideoInfo videoInfo) {
        try {
            String authorImage = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(authorImage) || !authorImage.startsWith("http")) {
                return false;
            }

            videoInfo.getAuthorInfo().setAuthorImage(authorImage);
            return true;
        } catch (Exception e) {
            logger.error("抽取作者头像异常:" + element.baseUri(), e);
        }
        return false;
    }

    /**
     * 抽取作者个人页面url
     * @author zjj, 2017-05-18
     * @param element
     * @param tags
     * @param videoInfo
     * @return
     */
    public static boolean getAuthorUrl(Element element, String tags, VideoInfo videoInfo) {
        try {
            String authorUrl = JsoupUtils.extractText(element, tags, 0);
            if (StringUtils.isBlank(authorUrl) || !authorUrl.startsWith("http")) {
                return false;
            }

            videoInfo.getAuthorInfo().setAuthorUrl(authorUrl);
            return true;
        } catch (Exception e) {
            logger.error("抽取作者头像异常:" + element.baseUri(), e);
        }
        return false;
    }





}
