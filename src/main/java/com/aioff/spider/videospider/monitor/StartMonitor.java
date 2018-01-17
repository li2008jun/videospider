package com.aioff.spider.videospider.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 开启监控线程类
 */
public class StartMonitor {
    private static Logger logger = LoggerFactory.getLogger(StartMonitor.class);

    /**
     * 开启监控线程
     * @param numThreads
     */
    public static void runMonitor(int numThreads){
        for(int i=0; i<numThreads; i++){
            new Thread(new Monitor()).start();
        }
        logger.info("开启监控线程" + numThreads + "个");
    }
}
