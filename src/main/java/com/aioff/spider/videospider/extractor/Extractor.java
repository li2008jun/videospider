package com.aioff.spider.videospider.extractor;

import com.aioff.spider.downloader.Downloader;
import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.downloader.SeleniumDownloader;
import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import com.aioff.spider.util.Sign;
import com.aioff.spider.util.TimeUtil;
import com.aioff.spider.util.UrlUtils;
import com.aioff.spider.videospider.db.mongo.VideoUrlMongoDAO;
import com.aioff.spider.videospider.entity.*;
import com.aioff.spider.videospider.modules.wangyi.VideoHandler_wangyi;
import com.aioff.spider.videospider.parse.ParseHandler;
import com.aioff.spider.videospider.pipeline.VideoPipeLine;
import com.aioff.spider.videospider.pool.DetailPool;
import com.aioff.spider.videospider.modules.toutiao.VideoHandler_toutiao;
import com.hankcs.hanlp.HanLP;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽取线程，从详情页缓存池中读取需要抓取的详情页信息，下载、解析并存储
 */
public class Extractor implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(Extractor.class);

    private static DetailPool detailPoolInstance = DetailPool.getInstance();

    private static VideoPipeLine videoPipeLineInstance = VideoPipeLine.getInstance();

    private static Downloader downloader = new HttpClientDownloader();
    private static SeleniumDownloader seleniumDownloader = new SeleniumDownloader();

    private static VideoUrlMongoDAO videoUrlMongoDAOInstance = VideoUrlMongoDAO.getInstance();

    private static FlvcdMovieUrlExtractor flvcdMovieUrlExtractor = new FlvcdMovieUrlExtractor();
    private static ShokdownMovieExtractor shokdownMovieExtractor = new ShokdownMovieExtractor();

    private static final String FLVCD = "flvcd";//获取视频真实地址用硕鼠
    private static final String SHOK_DOWN = "shokdown";//获取视频真实地址用舒克

    private static final long VIDEO_MAX_SIZE = 52428800;//最大52428800字节（50M）
    private static final long VIDEO_MIN_SIZE = 1048576;//最小1048576字节（1M）

    private static VideoHandler_toutiao videoHandler_toutiao = VideoHandler_toutiao.getInstance();
    private static VideoHandler_wangyi videoHandler_wangyi = VideoHandler_wangyi.getInstance();

    public void run() {
        logger.error("抽取线程 " + Thread.currentThread().getName() + " 启动");
        VideoInfo videoInfo = null;
        while (true) {
            try {
                logger.info(Thread.currentThread().getName() + "待抓取池： " + detailPoolInstance.getPoolSize());
                videoInfo = detailPoolInstance.get();
                if (videoInfo == null) {
                    logger.info(Thread.currentThread().getName() + " - Extrator线程休眠期……");
                    Thread.sleep(1000 * 5);
                } else {
                    parsePage(videoInfo);
                }
            } catch (Exception e) {
                logger.error("抽取线程发生错误:" + Thread.currentThread().getName(), e);
            }
        }
    }

    /**
     * 解析详情页，并下载视频首图，存储
     * @author zjj, 2017-05-18
     * @param videoInfo
     */
    public void parsePage(VideoInfo videoInfo) {
        if (videoInfo == null || StringUtils.isBlank(videoInfo.getSourceUrl())) {
            return;
        }

        logger.info(Thread.currentThread().getName() + "**** 开始处理正文页，url = " + videoInfo.getSourceUrl() + " ******");
        String mainHost = UrlUtils.getMainHost(videoInfo.getBoard().getUrl());
        if (StringUtils.isBlank(mainHost)) {
            return;
        }

        try {
            // 下载、解析列表页
            if (mainHost.contains(SiteEnum.TOUTIAO.getValue())) {
                // 今日头条
                videoHandler_toutiao.getVideoInfo(videoInfo);
            } else if (mainHost.contains(SiteEnum.MIAOPAI.getValue())) {
                // 秒拍-列表页接口中信息已全
            } else if (mainHost.contains(SiteEnum.WANGYI.getValue())) {
                // 网易
                videoHandler_wangyi.getVideoInfo(videoInfo);
            } else {
                // 普通网站-利用模板
                getVideoInfo(videoInfo);
            }
        } catch (Exception e) {
            logger.error("视频详情抽取异常：" + videoInfo.getSourceUrl(), e);
            videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
                    videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "视频详情抽取异常");
            return;
        }

        // 再利用硕鼠 or 舒克获取视频真实地址
        if (StringUtils.isBlank(videoInfo.getVideoUrl()) && videoInfo.getTemplate() != null
                && StringUtils.isNotBlank(videoInfo.getTemplate().getVideoUrlUtil())) {
            try {
                String video = null;
                Board board = videoInfo.getBoard();
                String sourceUrl = videoInfo.getSourceUrl();
                String videoUrlUtil = videoInfo.getTemplate().getVideoUrlUtil();

                if (FLVCD.equals(videoUrlUtil)) {
                    // 通过硕鼠获取视频真实地址
                    List<String> list = flvcdMovieUrlExtractor.getMovieUrl(sourceUrl);
                    if (list == null || list.size() == 0 || StringUtils.isBlank(list.get(0))) {
                        logger.warn("flvcd 视频地址抽取错误！" + sourceUrl);
                        videoUrlMongoDAOInstance.insertOrUpdate(sourceUrl, videoInfo.getTitle(),
                                board.getVideoSourceName(), board.getUrl(), "flvcd 视频地址抽取失败");
                        return;
                    }

                    video = list.get(0);
                    videoInfo.setVideoUrl(video);
                } else if (SHOK_DOWN.equals(videoUrlUtil)) {
                    // 通过舒克获取视频真实地址
                    List<String> list = shokdownMovieExtractor.getMovieUrlNoProxy(sourceUrl);
                    if (list == null || list.size() == 0 || StringUtils.isBlank(list.get(0))) {
                        logger.error("舒克 视频地址抽取错误！" + sourceUrl);
                        videoUrlMongoDAOInstance.insertOrUpdate(sourceUrl, videoInfo.getTitle(),
                                board.getVideoSourceName(), board.getUrl(), "舒克 视频地址抽取失败");
                        return;
                    }

                    video = list.get(0);
                    videoInfo.setVideoUrl(video);
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        
        // 筛选视频
        if (StringUtils.isNotBlank(videoInfo.get_id()) && StringUtils.isNotBlank(videoInfo.getTitle())
                && StringUtils.isNotBlank(videoInfo.getCover())
                && StringUtils.isNotBlank(videoInfo.getVideoUrl()) && videoInfo.getVideoUrl().startsWith("http")) {
            // 未采集到content，默认为标题
            if (StringUtils.isBlank(videoInfo.getContent())) {
                videoInfo.setContent(videoInfo.getTitle());
            }
            // 未采集到发布时间，默认为当前时间
            if (StringUtils.isBlank(videoInfo.getPublishTime())) {
                videoInfo.setPublishTime(TimeUtil.getCurrentTime());
            }
            videoInfo.setFetchTime(TimeUtil.getCurrentTime());
            videoInfo.setSequence(System.currentTimeMillis());
            
            /*关键字提取*/
            Map<String,Integer> keywordMap = new HashMap<String, Integer>();
            this.extractKeyword(keywordMap, videoInfo.getTitle());
            this.extractKeyword(keywordMap, videoInfo.getContent());
            videoInfo.setKeywordMap(keywordMap);

            logger.info("-------------------------------视频详细信息begin------------------------------------");
            logger.info("视频id：" + videoInfo.get_id());
            logger.info("视频详情页URL： " + videoInfo.getSourceUrl());
            logger.info("视频首图： " + videoInfo.getCover());
            logger.info("视频真实地址： " + videoInfo.getVideoUrl());
            logger.info("视频标题： " + videoInfo.getTitle());
            logger.info("视频时长： " + videoInfo.getDuration());
            logger.info("视频发布时间： " + videoInfo.getPublishTime());
            logger.info("视频简介： " + videoInfo.getContent());

            logger.info("视频作者ID： " + videoInfo.getAuthorInfo().getAuthorId());
            logger.info("视频作者名称： " + videoInfo.getAuthorInfo().getAuthorName());
            logger.info("视频作者头像： " + videoInfo.getAuthorInfo().getAuthorImage());
            logger.info("视频作者个人页面url： " + videoInfo.getAuthorInfo().getAuthorUrl());

            logger.info("视频播放量： " + videoInfo.getPlayCnt());
            logger.info("视频点赞数： " + videoInfo.getPraiseCnt());
            logger.info("视频评论数： " + videoInfo.getCommentCnt());
            logger.info("-------------------------------视频详细信息end------------------------------------");
            
            /*视频是否需要上传*/
            if (videoInfo.getBoard().getNeedUpload() == 0) {
                videoInfo.setNeedDownload(false);
            } else {
                videoInfo.setNeedDownload(true);
                if (!validVideoSize(videoInfo)) {
                    logger.info("视频大小不符要求：" + videoInfo.getSourceUrl());
                    videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
                            videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "视频大小不符要求");
                    return;
                }
            }
            
        } else {
            logger.info("视频抽取失败：" + videoInfo.getSourceUrl());
            videoUrlMongoDAOInstance.insertOrUpdate(videoInfo.getSourceUrl(), videoInfo.getTitle(),
                    videoInfo.getBoard().getVideoSourceName(), videoInfo.getBoard().getUrl(), "视频抽取失败");
            return;
        }

        // 持久化处理
        videoPipeLineInstance.process(videoInfo);

    }

    /**
     * 验证视频大小是否在允许范围内,增加视频大小video_size
     */
    private boolean validVideoSize(VideoInfo videoInfo) {
        String videoUrl = videoInfo.getVideoUrl();
        boolean flag = false;
        try {
            URL url = new URL(videoUrl);
            // 根据响应获取文件大小
            HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
            // 获取相应的文件长度
            long fileLength = urlcon.getContentLength();
            double video_size = fileLength / (1024 * 1024);
            videoInfo.setVideo_size(video_size);

            if (fileLength <= VIDEO_MAX_SIZE) {
                if (fileLength >= VIDEO_MIN_SIZE) {
                    flag = true;
                } else {
                    logger.info("------ 视频过小：" + videoUrl + "（" + fileLength / 1024 + "K）------");
                }
            } else {
                logger.info("------ 视频过大：" + videoUrl + "（" + fileLength / (1024 * 1024) + "M）------ ");
            }
        } catch (Exception e) {
            logger.info("------ 验证视频大小出错：" + videoUrl + "------");
        }
        return flag;
    }
    
    /**
     * 
     * @param keywordMap
     * @param content 内容
     * 
     */
    private void extractKeyword( Map<String,Integer> keywordMap,String content){
    	if(StringUtils.isNotEmpty(content)){
    		int size = content.length()/2;//假设关键字都是两个字，取商可计算个数
    		if(size != 0){
	        	List<String> titlekeywords = HanLP.extractKeyword(content, size);
	        	if(null != titlekeywords && !titlekeywords.isEmpty()){
	        		for(String keyword:titlekeywords){
	        			keywordMap.put(keyword, keywordMap.get(keyword)==null? 1 : keywordMap.get(keyword)+1);
	            	}
	        	}
    		}
        	
        }
    }


    /**
     * 获取视频详情页信息-普通网站，利用模板
     * @author zjj, 2017-05-22
     * @param videoInfo
     */
    private void getVideoInfo(VideoInfo videoInfo) {
        Board board = videoInfo.getBoard();
        Template template = videoInfo.getTemplate();
        String sourceUrl = videoInfo.getSourceUrl();
        videoInfo.set_id(Sign.getMD5(videoInfo.getSourceUrl()));

        if (template == null) {
            return;
        }

        // 下载源码
        Page page = null;
        try {
            if (board.getDynamic() == 2 || board.getDynamic() == 3) {
                page = seleniumDownloader.downloadSleep1s(new Request(sourceUrl));
            } else {
                page = downloader.download(new Request(sourceUrl));
            }
        } catch (Exception e) {
            logger.error("下载异常：" + sourceUrl, e);
            return;
        }

        String src = null;
        if (page == null || StringUtils.isBlank(page.getRawText())) {
            logger.error("源码下载失败：" + sourceUrl);
            videoUrlMongoDAOInstance.insertOrUpdate(sourceUrl, videoInfo.getTitle(),
                    board.getVideoSourceName(), board.getUrl(), "源码下载失败");
            return;
        }
        src = page.getRawText();

        // 解析详情页
        try {
            Document doc = Jsoup.parse(src, sourceUrl);
            String videoUrlUtil = template.getVideoUrlUtil();

            // 标题
            if (StringUtils.isBlank(videoInfo.getTitle()) && !ParseHandler.getTitle(doc, template.getDetailTitle(), videoInfo)) {
                logger.warn("抽取标题失败 - 详情页：" + sourceUrl);
                return;
            }
            // 简介
            if (StringUtils.isBlank(videoInfo.getContent()) && !ParseHandler.getContent(doc, template.getDetailContent(), videoInfo)) {
                logger.warn("抽取简介失败 - 详情页：" + sourceUrl);
                videoInfo.setContent(videoInfo.getTitle());
            }
            // 发布时间
            if (!ParseHandler.getTime(doc, template.getDetailTime(), videoInfo)) {
              //  System.out.println("抽取发布时间失败 - 详情页：" + sourceUrl);
                //logger.warn("抽取发布时间失败 - 详情页：" + sourceUrl);
                videoInfo.setPublishTime(TimeUtil.getCurrentTime());
            }
            // 时长(秒)
            if (videoInfo.getDuration() <= 0 && !ParseHandler.getDuration(doc, template.getDetailDuration(), videoInfo)) {
              //  System.out.println("抽取时长(秒)失败 - 详情页：" + sourceUrl);
                // logger.warn("抽取时长(秒)失败 - 详情页：" + sourceUrl);
            }

            // 作者名称
            if (!ParseHandler.getAuthorName(doc, template.getAuthorName(), videoInfo)) {
                logger.warn("抽取作者名称失败 - 详情页：" + sourceUrl);
            }
            // 作者ID
            if (!ParseHandler.getAuthorId(doc, template.getAuthorId(), videoInfo)) {
                //  logger.warn("抽取作者ID失败 - 详情页：" + sourceUrl);
            }
            // 作者头像
            if (!ParseHandler.getAuthorImage(doc, template.getAuthorImage(), videoInfo)) {
                logger.warn("抽取作者头像失败 - 详情页：" + sourceUrl);
            }
            // 作者个人页面url
            if (!ParseHandler.getAuthorUrl(doc, template.getAuthorUrl(), videoInfo)) {
                logger.warn("抽取作者个人页面url失败 - 详情页：" + sourceUrl);
            }

            // 播放数
            if (!ParseHandler.getPlayCnt(doc, template.getDetailPlayCnt(), videoInfo)) {
              //  System.out.println("抽取播放数失败 - 详情页：" + sourceUrl);
              //  logger.warn("抽取播放数失败 - 详情页：" + sourceUrl);
            }
            // 点赞数
            if (!ParseHandler.getPraiseCnt(doc, template.getDetailPraiseCnt(), videoInfo)) {
              //  System.out.println("抽取点赞数失败 - 详情页：" + sourceUrl);
               // logger.warn("抽取点赞数失败 - 详情页：" + sourceUrl);
            }
            // 踩数
            /*if (!ParseHandler.getTrampleCnt(doc, template.getDetailTrampleCnt(), videoInfo)) {
                logger.warn("抽取踩数失败 - 详情页：" + sourceUrl);
            }*/
            // 评论数
            if (!ParseHandler.getCommentCnt(doc, template.getDetailCommentCnt(), videoInfo)) {
              //  System.out.println("抽取评论数失败 - 详情页：" + sourceUrl);
                //logger.warn("抽取评论数失败 - 详情页：" + sourceUrl);
            }

            // 直接抽取视频真实地址
            if (StringUtils.isBlank(videoInfo.getVideoUrl()) && !ParseHandler.getVideoUrl(doc, template.getDetailVideoUrl(), videoInfo)) {
                //  logger.warn("直接抽取视频真实url失败 -  详情页：" + board.getUrl());
            }

            // 利用硕鼠 or 舒克获取视频真实地址
            if (StringUtils.isBlank(videoInfo.getVideoUrl())) {
                String video = null;
                if (FLVCD.equals(videoUrlUtil)) {
                    // 通过硕鼠获取视频真实地址
                    List<String> list = flvcdMovieUrlExtractor.getMovieUrl(sourceUrl);
                    if (list == null || list.size() == 0 || StringUtils.isBlank(list.get(0))) {
                        logger.warn("flvcd 视频地址抽取错误！" + sourceUrl);
                        videoUrlMongoDAOInstance.insertOrUpdate(sourceUrl, videoInfo.getTitle(),
                                board.getVideoSourceName(), board.getUrl(), "flvcd 视频地址抽取失败");
                        return;
                    }

                    video = list.get(0);
                    videoInfo.setVideoUrl(video);
                } else if (SHOK_DOWN.equals(videoUrlUtil)) {
                    // 通过舒克获取视频真实地址
                    List<String> list = shokdownMovieExtractor.getMovieUrlNoProxy(sourceUrl);
                    if (list == null || list.size() == 0 || StringUtils.isBlank(list.get(0))) {
                        logger.error("舒克 视频地址抽取错误！" + sourceUrl);
                        videoUrlMongoDAOInstance.insertOrUpdate(sourceUrl, videoInfo.getTitle(),
                                board.getVideoSourceName(), board.getUrl(), "舒克 视频地址抽取失败");
                        return;
                    }

                    video = list.get(0);
                    videoInfo.setVideoUrl(video);
                }
            }
        } catch (Exception e) {
            logger.error("解析详情页异常：" + sourceUrl, e);
            videoUrlMongoDAOInstance.insertOrUpdate(sourceUrl, videoInfo.getTitle(),
                    board.getVideoSourceName(), board.getUrl(), "解析详情页异常");
            return;
        }
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
    	VideoInfo videoInfo = null;
    	/*
    	 * new VideoInfo()
    	 * 
    	 */
        if (videoInfo != null) {
        	 new Extractor().parsePage(videoInfo);
        }else{
        	System.out.println("没有需要解析的详情页");
        }
    }

}
