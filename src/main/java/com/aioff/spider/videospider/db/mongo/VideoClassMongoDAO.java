package com.aioff.spider.videospider.db.mongo;

import com.aioff.clients.mongo.MongoClients;
import com.mongodb.client.MongoCollection;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * videoClass表操作
 */
public class VideoClassMongoDAO {
    private Logger logger = LoggerFactory.getLogger(VideoClassMongoDAO.class);

    private static final MongoCollection<Document> collection = MongoClients.getInstance().getDBCollection("videoClass");

    private static final VideoClassMongoDAO VIDEO_CLASS_MONGO_DAO = new VideoClassMongoDAO();

    public static VideoClassMongoDAO getInstance() {
        return VIDEO_CLASS_MONGO_DAO;
    }

    /**
     * 根据id获取分类名称
     */
    public String findNameById(String videoClassId) {
        if (StringUtils.isBlank(videoClassId)) {
            return null;
        }
        try {
            Document doc = collection.find(new Document("_id", videoClassId)).first();
            if (doc == null) {
                return null;
            } else {
                return doc.getString("name");
            }
        } catch (Exception e) {
            logger.error("VideoClassMongoDAO.findNameById() occur error!" + videoClassId, e);
            return null;
        }
    }
}
