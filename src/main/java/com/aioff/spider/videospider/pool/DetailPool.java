package com.aioff.spider.videospider.pool;

import com.aioff.spider.videospider.entity.VideoInfo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 详情页缓存池
 * Created by zhengjunjun on 2017/5/18.
 */
public class DetailPool {
    private static Logger logger = LoggerFactory.getLogger(DetailPool.class);

    private static LinkedList<VideoInfo> detailList = new LinkedList<VideoInfo>();

    private static Lock lock = new ReentrantLock();

    private static DetailPool detailPool = new DetailPool();

    public static DetailPool getInstance() {
        return detailPool;
    }

    /**
     * 获取需要抓取的详情页对象
     */
    public VideoInfo get(){
        lock.lock();
        try{
            if(detailList != null && detailList.size()>0){
                return detailList.removeFirst();
            }else{
                return null;
            }
        }finally{
            lock.unlock();
        }
    }


    /**
     * 增加新的详情页对象到缓存池中
     */
    public void add(VideoInfo videoInfo){
        lock.lock();
        try{
            detailList.add(videoInfo);
        } finally {
            lock.unlock();
        }
    }


    /**
     * 返回详情页池的大小
     */
    public int getPoolSize(){
        lock.lock();
        try{
            return detailList.size();
        }finally{
            lock.unlock();
        }
    }
}
