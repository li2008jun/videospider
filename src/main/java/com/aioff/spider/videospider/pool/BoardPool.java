package com.aioff.spider.videospider.pool;

import com.aioff.spider.videospider.db.mongo.VideoBoardMongoDAO;
import com.aioff.spider.videospider.entity.Board;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 列表页种子池
 */
public class BoardPool {
    private static Logger logger = LoggerFactory.getLogger(BoardPool.class);

    /** 种子列表 */
    private static LinkedList<Board> boardList = new LinkedList<Board>();
    /** 列表页url集合（用于去重） */
    public static Set<String> listCatch = new HashSet<String>();

    private static Lock lock = new ReentrantLock();//定义锁

    private static VideoBoardMongoDAO videoBoardMongoDAO = VideoBoardMongoDAO.getInstance();

    private static BoardPool boardPool;

    private BoardPool() {
        init();
    }

    public synchronized static BoardPool getInstance() {
        if (boardPool == null || CollectionUtils.isEmpty(boardList)) {
            boardPool = new BoardPool();
        }
        return boardPool;
    }


    /**
     * 初始化种子池
     */
    public void init(){
        //清理上次遍历留下的缓存
        boardList.clear();
        listCatch.clear();

        logger.info("正在初始化boardPool……");

        try {
            List<Board> queryList = videoBoardMongoDAO.query_crawler(10);
            if (CollectionUtils.isNotEmpty(queryList)) {
                for (Board board : queryList) {
                    if (listCatch.add(board.getUrl())) {
                        logger.info(board.toString());
                        boardList.add(board);
                    }
                }
            }
            logger.info("boardPool.size = " + boardList.size());
        } catch (Exception e) {
            logger.error("初始化种子池异常" + e);
        }
    }

    /**
     * 获取种子
     */
    public Board get(){
        lock.lock();
        try {
            if (CollectionUtils.isNotEmpty(boardList)) {
                return boardList.remove(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("获取Board对象时出错:" + e.getMessage());
        }finally{
            lock.unlock();
        }
        return null;
    }

    /**
     * 获取待抓取列表页缓存池大小
     * @return
     */
    public int getPoolSize() {
        if (boardList != null) {
            return boardList.size();
        } else {
            return 0;
        }
    }

}
