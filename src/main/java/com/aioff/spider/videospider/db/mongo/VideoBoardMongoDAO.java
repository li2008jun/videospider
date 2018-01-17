package com.aioff.spider.videospider.db.mongo;

import com.aioff.clients.mongo.MongoClients;
import com.aioff.spider.util.Sign;
import com.aioff.spider.videospider.entity.Board;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * videoBoard表操作
 * Created by zhengjunjun on 2017/5/18.
 */
public class VideoBoardMongoDAO {
    private static Logger logger = LoggerFactory.getLogger(VideoBoardMongoDAO.class);

    private static MongoCollection<Document> collection = MongoClients.getInstance().getDBCollection("videoBoard");

    private static final VideoBoardMongoDAO VIDEO_BOARD_MONGO_DAO = new VideoBoardMongoDAO();

    public static VideoBoardMongoDAO getInstance() {
        return VIDEO_BOARD_MONGO_DAO;
    }

    /**
     * 获取待抓取列表页信息
     * @auhtor zjj, 2017-05-17
     * @param num
     * @return
     */
    public List<Board> query_crawler(int num) {
        List<Board> resultList = new LinkedList<Board>();

        Document query = new Document();
        query.append("isStart", 1);//启动状态
        query.append("nextFetchTime", new Document("$lt", System.currentTimeMillis()));

        try {
            FindIterable<Document> docs = collection.find(query)
                    .sort(new Document("nextFetchTime", 1)).limit(num);
            if (docs != null) {
                for (Document doc : docs) {
                    Board board = convertToBoard(doc);
                    if (board != null) {
                        resultList.add(board);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("VideoBoardMongoDAO.query_crawler() occur error!", e);
        }

        return resultList;
    }
    
    public Board queryByUrl(String url){
    	Board board = null;
    	 Document query = new Document();
         query.append("isStart", 1);//启动状态
         query.append("url", url);

         try {
             Document doc = collection.find(query).sort(new Document("nextFetchTime", 1)).first();
             if (doc != null) {
            	 board = convertToBoard(doc);
             }
         } catch (Exception e) {
             logger.error("VideoBoardMongoDAO.queryByUrl() occur error!", e);
         }
    	return board;
    }

    /**
     * 将board表的MongoDB对象封装成boardUrl对象<p>
     * @author zjj, 2017-05-18
     * @param doc
     * @return
     * @author liqiang, 2015年4月8日
     *
     */
    private Board convertToBoard(Document doc) {
        if (doc == null) {
            return null;
        }

        Board board = new Board();

        board.set_id(doc.getString("_id"));
        board.setUrl(doc.getString("url"));
        board.setFromChannelName(doc.getString("fromChannelName"));
        board.setDynamic(doc.get("dynamic") == null ? 0 : doc.getInteger("dynamic", 0));
        board.setIsStart(doc.get("isStart") == null ? 0 : doc.getInteger("isStart", 1));
        board.setType(doc.get("type") == null ? 0 : doc.getInteger("type", 1));
        board.setVideoClassId(doc.getString("videoClassId"));
        board.setVideoClassName(doc.getString("videoClassName"));
        board.setVideoSourceId(doc.getString("videoSourceId"));
        board.setVideoSourceName(doc.getString("videoSourceName"));
        board.setFetchInterval(doc.getInteger("fetchInterval", 30));
        if (doc.get("nextFetchTime") == null) {
            board.setNextFetchTime(0);
        } else {
            board.setNextFetchTime(doc.get("nextFetchTime") instanceof Long ? (Long) doc.get("nextFetchTime")
                    : (Integer) doc.get("nextFetchTime"));
        }
        board.setMinLookCnt(doc.getInteger("minLookCnt", 0));

        board.setToutiao_category(doc.getString("toutiao_category"));
        board.setMiaopai_cateid(doc.getString("miaopai_cateid"));
        board.setWangyi_url(doc.getString("wangyi_url"));
        board.setMeipai_tid(doc.getString("meipai_tid"));
        board.setNeedUpload(doc.getInteger("needUpload", 0));

        return board;
    }

    /**
     * 更新nextFetchTime
     * @author zjj, 2017-05-18
     * @param id
     */
    public void updateNextFetchTime(String id, long nextFetchTime) {
        try {
            collection.updateOne(new Document("_id", id), new Document("$set", new Document("nextFetchTime", nextFetchTime)));
        } catch (Exception e) {
            logger.error("VideoBoardMongoDAO.updateNextFetchTime() occur error!", e);
        }
    }

    /**
     * 插入列表页模板
     * @author zjj, 2017-05-19
     * @param board
     */
    public void insert(Board board) {
        try {
            Document insertDoc = new Document();
            insertDoc.append("_id", board.get_id());
            insertDoc.append("url", board.getUrl());
            insertDoc.append("fromChannelName", board.getFromChannelName());
            insertDoc.append("dynamic", board.getDynamic());
            insertDoc.append("isStart", 0);
            insertDoc.append("type", 1);

            insertDoc.append("videoClassId", board.getVideoClassId());
            insertDoc.append("videoClassName", board.getVideoClassName());
            insertDoc.append("videoSourceId", board.getVideoSourceId());
            insertDoc.append("videoSourceName", board.getVideoSourceName());

            insertDoc.append("fetchInterval", 30);
            insertDoc.append("nextFetchTime", 0L);
            insertDoc.append("minLookCnt", 0);

            if (StringUtils.isNotBlank(board.getToutiao_category())) {
                insertDoc.append("toutiao_category", board.getToutiao_category());
            }
            if (StringUtils.isNotBlank(board.getMiaopai_cateid())) {
                insertDoc.append("miaopai_cateid", board.getMiaopai_cateid());
            }
            if (StringUtils.isNotBlank(board.getWangyi_url())) {
                insertDoc.append("wangyi_url", board.getWangyi_url());
            }
            if (StringUtils.isNotBlank(board.getMeipai_tid())) {
                insertDoc.append("meipai_tid", board.getMeipai_tid());
            }

            collection.insertOne(insertDoc);
        } catch (Exception e) {
            logger.error("VideoBoardMongoDAO.insert() occur error!", e);
        }
    }

    /**
     * 测试用
     * @param args
     */
    public static void main0(String[] args) {
        System.setProperty("spider.env", "");
        Board board = new Board();

        String url = "http://v.163.com/special/video/#tuijian";
        String id = Sign.getMD5(url);
        String fromChannelName = "推荐";
        int dynamic = 0;/////////
        //meipai.com miaopai.com 163.com toutiao.com
        String videoSourceId = "163.com";
        //美拍 秒拍 网易短视频 今日头条
        String videoSourceName = "网易短视频";

        //!!!今日头条独有字段
        String toutiao_category = "";
        //!!!秒拍独有字段
        String miaopai_cateid = "";
        //!!!美拍独有字段
        String meipai_tid = "";
        //!!!网易独有字段
        String wangyi_url = "http://v.163.com/special/video_tuijian/?callback=callback_video";

        board.set_id(id);
        board.setUrl(url);
        board.setFromChannelName(fromChannelName);
        board.setDynamic(dynamic);

        board.setVideoClassId("");
        board.setVideoClassName("");
        board.setVideoSourceId(videoSourceId);
        board.setVideoSourceName(videoSourceName);

        board.setToutiao_category(toutiao_category);
        board.setMiaopai_cateid(miaopai_cateid);
        board.setWangyi_url(wangyi_url);

        VIDEO_BOARD_MONGO_DAO.insert(board);
    }

    /**
     * 测试用
     * @param args
     */
    public static void main1(String[] args) {
        FindIterable<Document> docs = collection.find(new Document("videoSourceName", "美拍"));
        int num = 0;
        for (Document doc: docs) {
            String id = doc.getString("_id");
            System.out.println(doc);
            collection.updateOne(new Document("_id", id), new Document("$unset", new Document("meipai_tid", "")));
            num++;
        }
        System.out.println("num = " + num);
    }
}
