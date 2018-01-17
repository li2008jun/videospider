package com.aioff.spider.videospider.db.mongo;

import com.aioff.clients.mongo.MongoClients;
import com.aioff.spider.util.TimeUtil;
import com.aioff.spider.videospider.entity.VideoInfo;
import com.aioff.spider.videospider.upload.UploadOSS;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * videoInfo表操作
 */
public class VideoInfoMongoDAO {
    private Logger logger = LoggerFactory.getLogger(VideoInfo.class);

    private static final MongoCollection<Document> collection = MongoClients.getInstance().getDBCollection("videoInfo");

    private static final VideoInfoMongoDAO VIDEO_INFO_MONGO_DAO = new VideoInfoMongoDAO();

    public static VideoInfoMongoDAO getInstance() {
        return VIDEO_INFO_MONGO_DAO;
    }

    /**
     * 视频信息存入videoInfo表
     * @author zjj, 2017-05-18
     * @param videoInfo
     * @return
     */
    public synchronized boolean insert(VideoInfo videoInfo) {
        try {
            // _id去重
            if (collection.find(new Document("_id", videoInfo.get_id())).first() != null) {
                return true;
            }

            Document insertDoc = new Document();

            insertDoc.append("_id", videoInfo.get_id()); //id
            insertDoc.append("vid", System.currentTimeMillis()); //id

            insertDoc.append("title", videoInfo.getTitle()); //标题
            insertDoc.append("duration", videoInfo.getDuration()); //视频时长（秒）
            insertDoc.append("cover", videoInfo.getCover()); //视频的封面图片
            insertDoc.append("cover_local", videoInfo.getCover_local()); //视频的封面图片-本地图片路径
            insertDoc.append("cover_oss", videoInfo.getCover_oss()); //视频的封面图片上传OSS地址
            insertDoc.append("content", videoInfo.getContent()); //简介
            insertDoc.append("publishTime", videoInfo.getPublishTime()); //发布时间

            insertDoc.append("sourceUrl", videoInfo.getSourceUrl()); //爬虫获取详情页Url

            //作者信息
            Document authorInfo = new Document();
            authorInfo.append("authorId", videoInfo.getAuthorInfo().getAuthorId()); //作者id
            authorInfo.append("authorName", videoInfo.getAuthorInfo().getAuthorName()); //作者名称
            authorInfo.append("authorImage", videoInfo.getAuthorInfo().getAuthorImage()); //作者头像
            authorInfo.append("authorUrl", videoInfo.getAuthorInfo().getAuthorUrl()); //作者个人页面url
            insertDoc.append("authorInfo", authorInfo);

            insertDoc.append("fetchTime", videoInfo.getFetchTime()); //抓取时间
            insertDoc.append("sequence", videoInfo.getSequence()); //抓取时间戳

            insertDoc.append("playCnt", videoInfo.getPlayCnt()); //播放数
            insertDoc.append("praiseCnt", videoInfo.getPraiseCnt()); //点赞数
            insertDoc.append("trampleCnt", videoInfo.getTrampleCnt()); //踩数
            insertDoc.append("commentCnt", videoInfo.getCommentCnt()); //评论数

            insertDoc.append("boardUrl", videoInfo.getBoard().getUrl()); //所属列表页url
            insertDoc.append("fromChannelName", videoInfo.getBoard().getFromChannelName());//爬虫获取数据频道(原网站的视频版块名)

            insertDoc.append("videoClassId", videoInfo.getBoard().getVideoClassId()); //视频分类Id
            insertDoc.append("videoClassName", videoInfo.getBoard().getVideoClassName()); //视频分类名称

            insertDoc.append("videoSourceId", videoInfo.getBoard().getVideoSourceId()); //视频来源id
            insertDoc.append("videoSourceName", videoInfo.getBoard().getVideoSourceName()); //视频来源站点名称

            insertDoc.append("video_size", videoInfo.getVideo_size());
            if (videoInfo.isNeedDownload()) {
                insertDoc.append("status", 0); //状态 0代表不可用，1代表正常
                insertDoc.append("isDownload", 1); //视频是否下载到本地 1:是 0:否
                insertDoc.append("downloadTime", TimeUtil.getCurrentTime()); //下载完成时间

                insertDoc.append("videoUrl", ""); //需下载上传的，视频真实地址置空，待上传成功后回写
                insertDoc.append("video_local", videoInfo.getVideo_local()); //视频下载后的本地路径
            } else {
                insertDoc.append("status", 1); //状态 0代表不可用，1代表正常
                insertDoc.append("isDownload", 0);
                insertDoc.append("downloadTime", "");

                insertDoc.append("videoUrl", videoInfo.getVideoUrl()); //视频真实地址
                insertDoc.append("video_local", videoInfo.getVideo_local()); //视频下载后的本地路径
            }

            insertDoc.append("isUp", 0); //视频是否上传 1:是 0:否
            insertDoc.append("upTime", ""); //上传完成时间
            insertDoc.append("videoId", ""); //视频上传云后返回id

            // 今日头条保存字段
            if (StringUtils.isNotBlank(videoInfo.getToutiao_group_id())) {
                insertDoc.append("toutiao_group_id", videoInfo.getToutiao_group_id());
            }
            if (StringUtils.isNotBlank(videoInfo.getToutiao_video_id())) {
                insertDoc.append("toutiao_video_id", videoInfo.getToutiao_video_id());
            }

            // 附加字段
            long temp = 0L;
            insertDoc.append("comm_nums", temp);
            insertDoc.append("biglike", temp);
            insertDoc.append("totalcount", temp);
            insertDoc.append("switch", temp);
            
            // 关键字
            Map<String,Integer> keywordMap = videoInfo.getKeywordMap();
            if(null != keywordMap && !keywordMap.isEmpty()){
            	List<String> keywords = new ArrayList<String>();
            	for(String keyword : keywordMap.keySet()){
            		keywords.add(keyword);
            	}
            	insertDoc.append("keywords", keywords);
            }
            collection.insertOne(insertDoc);
            return true;
        } catch (Exception e) {
            logger.error("VideoInfoMongoDAO.insert() occur error!", e);
        }

        return false;
    }

    /**
     * 测试用
     * @param args
     */
    public static void main(String[] args) {
        FindIterable<Document> docs = collection.find(new Document().append("fetchTime", new Document("$gte", "2017-07-11 21:23:42"))
                .append("status", new Document("$eq", 0)))
                .sort(new Document("fetchTime", -1));
        int i = 0;
        for (Document doc: docs) {
            i++;
            String id = doc.getString("_id");
            String fetchTime = doc.getString("fetchTime");

            String videoUrl = doc.getString("videoUrl");
            System.out.println(i + ": " + fetchTime + ", " + id + ", " + videoUrl);

            if (StringUtils.isNotBlank(videoUrl) && videoUrl.contains(".mp4")) {
                collection.updateOne(new Document("_id", id), new Document("$set", new Document("status", 1)));
            }



        }
    }

    public static void main2(String[] args) {
        System.out.println(System.currentTimeMillis() + new Random().nextInt(1000));
        System.out.println(Long.MIN_VALUE);
    }

}
