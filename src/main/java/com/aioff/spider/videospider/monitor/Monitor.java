package com.aioff.spider.videospider.monitor;

import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import com.aioff.spider.downloader.Downloader;
import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.downloader.SeleniumDownloader;
import com.aioff.spider.util.Sign;
import com.aioff.spider.util.UrlUtils;
import com.aioff.spider.videospider.db.mongo.VideoBoardMongoDAO;
import com.aioff.spider.videospider.db.mongo.VideoTemplateMongoDAO;
import com.aioff.spider.videospider.db.mongo.VideoUrlMongoDAO;
import com.aioff.spider.videospider.modules.meipai.ListHandler_meipai;
import com.aioff.spider.videospider.modules.miaopai.ListHandler_miaopai;
import com.aioff.spider.videospider.modules.wangyi.ListHandler_wangyi;
import com.aioff.spider.videospider.parse.ParseHandler;
import com.aioff.spider.videospider.pool.DetailPool;
import com.aioff.spider.videospider.modules.toutiao.ListHandler_toutiao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aioff.spider.videospider.bloom.biz.BloomFilterBiz;
import com.aioff.spider.videospider.entity.*;
import com.aioff.spider.videospider.extractor.Extractor;
import com.aioff.spider.videospider.pool.BoardPool;

import java.util.ArrayList;
import java.util.List;

/**
 * 监控线程，用来遍历种子，发现详情页放入DetailPool
 */
public class Monitor implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(Monitor.class);

    private static BoardPool boardPoolInstance = BoardPool.getInstance();
    private static DetailPool detailPoolInstance = DetailPool.getInstance();

    private static BloomFilterBiz bloomFilterBizInstance = BloomFilterBiz.getInstance();

    private static Downloader downloader = new HttpClientDownloader();
    private static SeleniumDownloader seleniumDownloader = new SeleniumDownloader();

    private static VideoBoardMongoDAO videoBoardMongoDAOInstance = VideoBoardMongoDAO.getInstance();
    private static VideoTemplateMongoDAO videoTemplateMongoDAOInstance = VideoTemplateMongoDAO.getInstance();
    private static VideoUrlMongoDAO videoUrlMongoDAOInstance = VideoUrlMongoDAO.getInstance();

    private static ListHandler_toutiao listHandler_toutiao = ListHandler_toutiao.getInstance();
    private static ListHandler_miaopai listHandler_miaopai = ListHandler_miaopai.getInstance();
    private static ListHandler_wangyi listHandler_wangyi = ListHandler_wangyi.getInstance();
    private static ListHandler_meipai listHandler_meipai = ListHandler_meipai.getInstance();

    public void run() {
        logger.info("监控线程" + Thread.currentThread().getName() + " 启动");
        Board board = null;

        while (true) {
            try {
                logger.info(Thread.currentThread().getName() + " 待抓取种子池数量：" + boardPoolInstance.getPoolSize());
                board = boardPoolInstance.get();

                if (board == null) {
                    logger.info(Thread.currentThread().getName() + " - Monitor线程休眠期……");
                    Thread.sleep(1000 * 15);
                    // 重新初始化种子池
                    boardPoolInstance = BoardPool.getInstance();
                } else {
                    // 解析列表页
                    parseList(board);
                }
            } catch (Exception e) {
                logger.error("监控线程发生错误:" + Thread.currentThread().getName(), e);
            }
        }
    }

    /**
     * 下载并解析列表页
     */
    private void parseList(Board board) {
        if (board == null || StringUtils.isBlank(board.getUrl())) {
            return;
        }

        logger.info(Thread.currentThread().getName() + "****** 开始下载并解析列表页： " + board.getUrl() + " ******");
        String mainHost = UrlUtils.getMainHost(board.getUrl());
        if (StringUtils.isBlank(mainHost)) {
            return;
        }

        int numNewUrl = 0;
        List<VideoInfo> videoInfoList = new ArrayList<VideoInfo>();

        try {
            // 下载、解析列表页
            if (mainHost.contains(SiteEnum.TOUTIAO.getValue())) {
                // 今日头条
                videoInfoList = listHandler_toutiao.getListInfo(board, 0 , 1);
            } else if (mainHost.contains(SiteEnum.MIAOPAI.getValue())) {
                // 秒拍
                videoInfoList = listHandler_miaopai.getListInfo(board, 1);
            } else if (mainHost.contains(SiteEnum.WANGYI.getValue())) {
                // 网易短视频
                videoInfoList = listHandler_wangyi.getListInfo(board);
            } else if (mainHost.contains(SiteEnum.MEIPAI.getValue())) {
                // 美拍
                videoInfoList = listHandler_meipai.getListInfo(board, 1);
            } else {
                // 普通网站-利用模板
                videoInfoList = getListInfo(board);
            }

            // url过滤、去重
            if (CollectionUtils.isEmpty(videoInfoList)) {
                logger.warn("列表页无数据！boardUrl = " + board.getUrl());
            } else {
                for (VideoInfo videoInfo : videoInfoList) {
                    // 初步过滤url
                    String sourceUrl = videoInfo.getSourceUrl();
                    if (StringUtils.isBlank(sourceUrl) || sourceUrl.contains("javascript")) {
                        continue;
                    }
                    // 布隆去重
                    if (!bloomFilterBizInstance.containUrl(sourceUrl)) {
                        detailPoolInstance.add(videoInfo);
                        numNewUrl++;
                    }
                }
            }

            logger.info("列表页 {} 发现 {} 个视频，其中被去重 {} 个，新增 {} 个", board.getUrl(), videoInfoList==null?0:videoInfoList.size(),
                    videoInfoList==null?0:(videoInfoList.size() - numNewUrl), numNewUrl);
        } catch (Exception e) {
            logger.error("解析列表页异常！boardUrl = " + board.getUrl(), e);
        }

        // 更新nextFetchTime
        long nextFetchTime = System.currentTimeMillis() + 1000 * 60 * board.getFetchInterval();
        videoBoardMongoDAOInstance.updateNextFetchTime(board.get_id(), nextFetchTime);
    }


    /**
     * 下载，解析视频列表接口 - 普通网站，利用模板
     * @author zjj, 2017-05-22
     * @param board
     * @return
     */
    private List<VideoInfo> getListInfo(Board board) {
        List<VideoInfo> videoInfoList = new ArrayList<VideoInfo>();
        // 加载模板
        Template template = videoTemplateMongoDAOInstance.find(board.getVideoSourceId());
        if (template == null) {
            logger.info("模板未找到！url = " + board.getUrl());
            return null;
        }

        // 下载源码
        Page page = null;
        try {
            if (board.getDynamic() == 0 || board.getDynamic() == 2) {
                page = downloader.download(new Request(board.getUrl()));
            } else {
                page = seleniumDownloader.downloadSleep1s(new Request(board.getUrl()));
            }
        } catch (Exception e) {
            logger.error("下载异常：" + board.getUrl(), e);
            return null;
        }

        String src = null;
        if (page == null || StringUtils.isBlank(page.getRawText())) {
            logger.error("源码下载失败：" + board.getUrl());
            return null;
        }
        src = page.getRawText();

        // 解析列表页
        try {
            Document doc = Jsoup.parse(src, board.getUrl());

            String listDivMatch = template.getListDiv();
            if (StringUtils.isBlank(listDivMatch)) {
                logger.error("模板中listDiv为空！" + template.get_id());
                return null;
            }

            // 筛选出视频列表中单个视频模块
            Elements elements = doc.select(listDivMatch);
            int numNewUrl = 0;
            if (elements == null || elements.size() == 0) {
                logger.error("videoDiv 模板出错,无匹配数据！url = " + board.getUrl());
                return null;
            }

            for (Element element : elements) {
                VideoInfo videoInfo = new VideoInfo();
                videoInfo.setBoard(board);
                videoInfo.setTemplate(template);
                AuthorInfo authorInfo = new AuthorInfo();
                videoInfo.setAuthorInfo(authorInfo);

                // 详情页url
                if (!ParseHandler.getListSourceUrl(element, template.getListSourceUrl(), videoInfo)) {
                    logger.warn("抽取详情页url失败 - 列表页：" + board.getUrl());
                    continue;
                }
                // 初步过滤url
                String sourceUrl = videoInfo.getSourceUrl();
                if (StringUtils.isBlank(sourceUrl) || sourceUrl.contains("javascript")) {
                    continue;
                }
                videoInfo.set_id(Sign.getMD5(videoInfo.getSourceUrl()));

                // 视频真实url
                if (!ParseHandler.getVideoUrl(element, template.getListVideoUrl(), videoInfo)) {
                   // System.out.println("直接抽取视频真实url失败 - 列表页：" + board.getUrl());
                    //  logger.warn("直接抽取视频真实url失败 - 列表页：" + board.getUrl());
                }

                // 标题
                if (!ParseHandler.getTitle(element, template.getListTitle(), videoInfo)) {
                    logger.warn("抽取标题失败 - 列表页：" + board.getUrl());
                }

                // 封面图片
                if (!ParseHandler.getCover(element, template.getListCover(), videoInfo)) {
                    logger.warn("抽取封面图片失败 - 列表页：" + board.getUrl());
                    continue;
                }

                // 发布时间
                if (!ParseHandler.getTime(element, template.getListTime(), videoInfo)) {
                  //  System.out.println("抽取发布时间失败 - 列表页：" + board.getUrl());
                 //   logger.warn("抽取发布时间失败 - 列表页：" + board.getUrl());
                }

                // 播放数
                if (!ParseHandler.getPlayCnt(element, template.getListPlayCnt(), videoInfo)) {
                  //  System.out.println("抽取播放数失败 - 列表页：" + board.getUrl());
                 //   logger.warn("抽取播放数失败 - 列表页：" + board.getUrl());
                }
                // 点赞数
                if (!ParseHandler.getPraiseCnt(element, template.getListPraiseCnt(), videoInfo)) {
                 //   System.out.println("抽取点赞数失败 - 列表页：" + board.getUrl());
                 //   logger.warn("抽取点赞数失败 - 列表页：" + board.getUrl());
                }
                // 评论数
                if (!ParseHandler.getCommentCnt(element, template.getListCommentCnt(), videoInfo)) {
                 //   System.out.println("抽取评论数失败 - 列表页：" + board.getUrl());
                 //   logger.warn("抽取评论数失败 - 列表页：" + board.getUrl());
                }

                videoInfoList.add(videoInfo);
            }
        } catch (Exception e) {
            logger.error("解析列表页时出错! url = " + board.getUrl());
        }

        return videoInfoList;
    }
    
    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
		String url1 = "http://v.163.com/special/video/#gaoxiao";
		String url2 = "http://www.toutiao.com/ch/subv_funny/";
		Board board = videoBoardMongoDAOInstance.queryByUrl(url2);
		if (board != null) {
			new Monitor().parseList(board);
		} else {
			System.out.println("没有需要解析的任务");
		}
		VideoInfo videoInfo = null;
		Extractor extractor = new Extractor();
		while (true) {
			videoInfo = detailPoolInstance.get();
			if (videoInfo != null) {
				extractor.parsePage(videoInfo);
			} else {
				System.out.println("没有需要解析的详情页");
				break;
			}
		}
    }

}
