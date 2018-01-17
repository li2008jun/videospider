package com.aioff.spider.videospider;

import com.aioff.spider.videospider.util.SysConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aioff.spider.videospider.extractor.StartExtractor;
import com.aioff.spider.videospider.monitor.StartMonitor;

/**
 * 视频爬虫主入口
 */
public class SpiderMain {
    private static Logger logger = LoggerFactory.getLogger(SpiderMain.class);

    public static void main(String[] args) {
        System.setProperty("spider.env", "");

        int monitor_thread_num = SysConfig.getInstance().getIntegerVal("monitor_thread_num");
        int extractor_thread_num = SysConfig.getInstance().getIntegerVal("extractor_thread_num");

        logger.info("【开启监控线程 {} 个，抽取下载线程 {} 个】", monitor_thread_num, extractor_thread_num);

        //启动监控线程
        StartMonitor.runMonitor(monitor_thread_num);

        //启动抽取下载线程
        StartExtractor.runExtractor(extractor_thread_num);

    }

}
