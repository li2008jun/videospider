package com.aioff.spider.videospider.db.mongo;

import com.aioff.clients.mongo.MongoClients;
import com.aioff.spider.util.Sign;
import com.aioff.spider.util.TimeUtil;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * videoUrl表操作
 * Created by zhengjunjun on 2017/5/16.
 */
public class VideoUrlMongoDAO {
    private static Logger logger = LoggerFactory.getLogger(VideoUrlMongoDAO.class);

    private static MongoCollection<Document> collection = MongoClients.getInstance().getDBCollection("videoUrl");

    private static final VideoUrlMongoDAO VIDEO_URL_MONGO_DAO = new VideoUrlMongoDAO();

    public static VideoUrlMongoDAO getInstance() {
        return VIDEO_URL_MONGO_DAO;
    }

    /**
     * 无-插入记录，有-更新tips
     * @param url
     * @param title
     * @param hostName
     * @param boardUrl
     * @param tips
     */
    public void insertOrUpdate(String url, String title, String hostName, String boardUrl, String tips) {
        try {
            String id = Sign.getMD5(url);
            Document doc = collection.find(new Document("_id", id)).first();
            if (doc != null && url.equals(doc.getString("url"))) {
                collection.updateOne(new Document("_id", id), new Document("$set", new Document("tips", tips)
                        .append("updateTime", TimeUtil.getCurrentTime())));
                return;
            }

            Document insertDoc = new Document();
            insertDoc.append("_id", id).append("url", url).append("title", title);
            insertDoc.append("hostName", hostName).append("boardUrl", boardUrl);
            insertDoc.append("tips", tips);
            insertDoc.append("fetchTime", System.currentTimeMillis()).append("updateTime", TimeUtil.getCurrentTime());
            collection.insertOne(insertDoc);
        } catch (Exception e) {
            logger.error("VideoUrlMongoDAO.insertOrUpdate() occur error!", e);
        }
    }

}
