package com.aioff.spider.videospider.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动抽取线程
 */
public class StartExtractor {
    private static Logger logger = LoggerFactory.getLogger(StartExtractor.class);

    /**
     * 启动抽取线程
     * @param numThreads
     */
    public static void runExtractor(int numThreads){
        for(int i= 0; i<numThreads; i++){
            new Thread(new Extractor()).start();
        }
        logger.info("开启抽取线程"+numThreads+"个");
    }
}
