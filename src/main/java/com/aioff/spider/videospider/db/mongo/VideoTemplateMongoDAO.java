package com.aioff.spider.videospider.db.mongo;

import com.aioff.clients.mongo.MongoClients;
import com.aioff.spider.util.Environment;
import com.aioff.spider.videospider.entity.Template;
import com.mongodb.client.MongoCollection;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 视频正文页模板videoTemplate表操作
 * Created by zhengjunjun on 2017/5/16.
 */
public class VideoTemplateMongoDAO {
    private Logger logger = LoggerFactory.getLogger(VideoTemplateMongoDAO.class);

    private static MongoCollection<Document> collection = MongoClients.getInstance().getDBCollection("videoTemplate");

    private static final VideoTemplateMongoDAO VIDEO_TEMPLATE_MONGO_DAO = new VideoTemplateMongoDAO();

    public static VideoTemplateMongoDAO getInstance() {
        return VIDEO_TEMPLATE_MONGO_DAO;
    }

    /**
     * 获取视频正文页模板，并封装为Template对象
     * @author zhengjunjun, 2017-05-18
     * @param id
     * @return
     */
    public Template find(String id) {
        Template template = new Template();
        if (StringUtils.isBlank(id)) {
            return null;
        }
        try {
            Document doc = collection.find(new Document("_id", id)).first();
            if (doc == null) {
                return null;
            } else {
                template.set_id(doc.getString("_id"));
                template.setName(doc.getString("name"));

                template.setListDiv(doc.getString("listDiv"));
                template.setListTitle(doc.getString("listTitle"));
                template.setListCover(doc.getString("listCover"));
                template.setListSourceUrl(doc.getString("listSourceUrl"));
                template.setListVideoUrl(doc.getString("listVideoUrl"));
                template.setListTime(doc.getString("listTime"));
                template.setListPlayCnt(doc.getString("listPlayCnt"));
                template.setListPraiseCnt(doc.getString("listPraiseCnt"));
                template.setListCommentCnt(doc.getString("listCommentCnt"));

                template.setDetailTitle(doc.getString("detailTitle"));
                template.setDetailContent(doc.getString("detailContent"));
                template.setDetailDuration(doc.getString("detailDuration"));
                template.setDetailTime(doc.getString("detailTime"));
                template.setDetailVideoUrl(doc.getString("detailVideoUrl"));

                template.setDetailPlayCnt(doc.getString("detailPlayCnt"));
                template.setDetailPraiseCnt(doc.getString("detailPraiseCnt"));
                template.setDetailTrampleCnt(doc.getString("detailTrampleCnt"));
                template.setDetailCommentCnt(doc.getString("detailCommentCnt"));

                template.setVideoUrlUtil(doc.getString("videoUrlUtil"));

                template.setAuthorId(doc.getString("authorId"));
                template.setAuthorName(doc.getString("authorName"));
                template.setAuthorImage(doc.getString("authorImage"));
                template.setAuthorUrl(doc.getString("authorUrl"));
            }
        } catch (Exception e) {
            logger.error("VideoTemplateMongoDAO.find() occur error: " + id, e);
            return null;
        }
        return template;
    }

    /**
     * 插入详情页模板
     * @author zjj, 2017-05-19
     * @param template
     */
    public void insert(Template template) {
        try {
            Document insertDoc = new Document();
            insertDoc.append("_id", template.get_id());
            insertDoc.append("name", template.getName());

            insertDoc.append("listDiv", "");
            insertDoc.append("listTitle", "");
            insertDoc.append("listCover", "");
            insertDoc.append("listSourceUrl", "");
            insertDoc.append("listVideoUrl", "");
            insertDoc.append("listTime", "");
            insertDoc.append("listPlayCnt", "");
            insertDoc.append("listPraiseCnt", "");
            insertDoc.append("listCommentCnt", "");

            insertDoc.append("detailTitle", "");
            insertDoc.append("detailContent", "");
            insertDoc.append("detailDuration", "");
            insertDoc.append("detailTime", "");
            insertDoc.append("detailVideoUrl", "");

            insertDoc.append("detailPlayCnt", "");
            insertDoc.append("detailCommentCnt", "");
            insertDoc.append("detailPraiseCnt", "");
            insertDoc.append("detailTrampleCnt", "");

            insertDoc.append("videoUrlUtil", "");

            insertDoc.append("authorId", "");
            insertDoc.append("authorName", "");
            insertDoc.append("authorImage", "");
            insertDoc.append("authorUrl", "");

            collection.insertOne(insertDoc);
        } catch (Exception e) {
            logger.error("VideoTemplateMongoDAO.insert() occur error!", e);
        }
    }

    public static void main(String[] args) {
       // System.setProperty("spider.env", Environment.getEnv(null));
        String id = "365yg.com";
        String name = "阳关宽频网";

        Template temmlate = new Template();
        temmlate.set_id(id);
        temmlate.setName(name);
        VideoTemplateMongoDAO.getInstance().insert(temmlate);
    }

}
