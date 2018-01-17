package com.aioff.spider.videospider.pipeline;

import com.aioff.clients.solr.SolrClients;
import com.aioff.clients.solr.model.consts.SolrType;
import com.aioff.spider.downloader.Downloader;
import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import com.aioff.spider.util.Sign;
import com.aioff.spider.util.UrlUtils;
import com.aioff.spider.videospider.db.mongo.VideoInfoMongoDAO;
import com.aioff.spider.videospider.db.mongo.VideoUrlMongoDAO;
import com.aioff.spider.videospider.download.ImageDownloader;
import com.aioff.spider.videospider.download.VideoDownloader;
import com.aioff.spider.videospider.entity.SiteEnum;
import com.aioff.spider.videospider.entity.VideoInfo;
import com.aioff.spider.videospider.upload.UploadOSS;
import com.aioff.spider.videospider.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 视频数据存储（下载图片）
 * Created by zhengjunjun on 2017/5/19.
 */
public class VideoPipeLine {
    private static Logger logger = LoggerFactory.getLogger(VideoPipeLine.class);

    private static Downloader downloader = new HttpClientDownloader();

    private static ImageDownloader imageDownloader = ImageDownloader.getInstance();
    private static VideoDownloader videoDownloader = VideoDownloader.getInstance();

    private static VideoInfoMongoDAO videoInfoMongoDAOInstance = VideoInfoMongoDAO.getInstance();
    private static VideoUrlMongoDAO videoUrlMongoDAOInstance = VideoUrlMongoDAO.getInstance();

    public static VideoPipeLine getInstance() {
        return new VideoPipeLine();
    }

    /**
     * 视频下载图片，存储MongoDB和Solr
     * @author zjj, 2017-05-19
     * @param videoInfo
     */
    public boolean process(VideoInfo videoInfo) {
        if (videoInfo == null) {
            return false;
        }

        // 下载封面图片，名称=图片url的MD5签名
        try {
            if (StringUtils.isBlank(videoInfo.getCover())) {
                logger.warn("封面图片为空");
                return false;
            }

            // 生成图片名称
            String imageName = Sign.getMD5(videoInfo.getCover()) + "." + FileUtil.getImageSuffix(videoInfo.getCover());
            String filePath = imageDownloader.downloadImage(videoInfo.getCover(), imageName, videoInfo.getSourceUrl());
            videoInfo.setCover_local(filePath);
            if (StringUtils.isBlank(filePath)) {
                logger.warn("封面图片下载失败, imageUrl = " + videoInfo.getCover());
                videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
                        videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "图片下载失败");
                return false;
            }
        } catch (Exception e) {
            logger.error("封面图片下载异常, imageUrl = " + videoInfo.getCover(), e);
            videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
                    videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "图片下载异常");
            return false;
        }

        // 上传图片至OSS
        String cover_oss = UploadOSS.uploadImage(videoInfo.getCover_local());
        if (StringUtils.isBlank(cover_oss)) {
            logger.warn("封面图片上传失败, imagePath = " + videoInfo.getCover_local());
            videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
                    videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "图片上传失败");
            //删除本地图片
            imageDownloader.deleteImage(videoInfo.getCover_local());
            return false;
        } else {
            videoInfo.setCover_oss(cover_oss);
        }

        //删除本地图片
        imageDownloader.deleteImage(videoInfo.getCover_local());

        // 下载视频到本地
        if (videoInfo.isNeedDownload()) {
            String mainHost = UrlUtils.getMainHost(videoInfo.getBoard().getUrl());
            String video_local = null;
            if (StringUtils.isNotBlank(mainHost) && mainHost.contains(SiteEnum.MIAOPAI.getValue())) {
                // 秒拍需获取跳转地址
                String videoUrl_target = null;
                Page page = null;
                try {
                    page = HttpClientDownloader.DEFAULT().download(new Request(videoInfo.getVideoUrl()));
                    if (page != null) {
                        videoUrl_target = page.getTargetUrl();
                    }
                } catch (Exception e) {
                }

                if (StringUtils.isNotBlank(videoUrl_target)) {
                    video_local = videoDownloader.downloadVideo(videoUrl_target, videoInfo.get_id());
                }
            } else {
                video_local = videoDownloader.downloadVideo(videoInfo.getVideoUrl(), videoInfo.get_id());
            }

            if (StringUtils.isBlank(video_local)) {
                logger.warn("视频下载失败, videoPath = " + video_local);
                videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
                        videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "视频下载失败");
                return false;
            } else {
                videoInfo.setVideo_local(video_local);
            }
        } else {
        	/*
        	 * 下面代码暂时注释，例如下面头条视频，虽然不是以mp4结尾，但是依然可以播放
        	 *http://v3-tt.ixigua.com/db72c8c9a9504ce62d38a6628e06f19e/59b694ca/video/m/2203eb29d90eeb64ee7ab517af04d9bba8b114929700003fb7d9068e0c/
        	 * 
        	 */
        	// 不下载视频的，核查为.mp4
//            if (!videoInfo.getVideoUrl().contains(".mp4")) {
//                logger.warn("非mp4, videoUrl = " + videoInfo.getVideoUrl());
//                videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
//                        videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "视频非mp4");
//                return false;
//            }
        }

        // 存储MongoDB
        if (!videoInfoMongoDAOInstance.insert(videoInfo)) {
            logger.error("存储MongoDB失败, sourceUrl = " + videoInfo.getSourceUrl());
            videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
                    videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "存储MongoDB失败");
            return false;
        }

        logger.info("【视频存储成功, " + videoInfo.getBoard().getVideoSourceName() + ", sourceUrl = " + videoInfo.getSourceUrl() + "】");
        videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
                videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "视频存储成功");
        return true;
    }

}
