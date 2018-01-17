package com.aioff.spider.videospider.bloom.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aioff.spider.videospider.bloom.BloomFilter;
import com.aioff.spider.Constant;

/**
 * 布隆过滤器的业务层
 */
public class BloomFilterBiz {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private BloomFilter filter = BloomFilter.getInstance();

	private static BloomFilterBiz bloomBiz = new BloomFilterBiz();

	private BloomFilterBiz() {
	}

	public static BloomFilterBiz getInstance() {
		return bloomBiz;
	}

	/**
	 * 
	 * 方法作用:对传递过来的集合进行布隆去重,然后将新发现的帖子入库，再同时增加到布隆的配置文件中
	 * 	             这三个步骤应该是连贯的而不是彼此独立的
	 * @param urls
	 * @return
	 */
	/*public synchronized int filterUrls(ArrayList<NeedFetchUrl> urls) {

		int count = 0;
		for (NeedFetchUrl url : urls) {
			try {
				// 如果布隆配置文件中不包含此url
				if (!filter.contains(url.getUrl())) {
					// 插入mongodb
					if (mongo.insertNeedFetchUrl(url)) {
						// 插入布隆配置文件
						filter.insert(url.getUrl());
						// Common.writeFile(Constant.BLOOM_FILTER_FILE,
						// url.getUrl());
						filter.writeBloomUrl(url.getUrl(),
								Constant.BLOOM_FILTER_FILE);
						count++;

						System.out.println("发现新帖子 : " + url.getUrl()
								+ "    标题 : " + url.getTitle());
					} else {
						// mongodb中已经存在此帖子，则将帖子插入布隆过滤器
						filter.insert(url.getUrl());
						filter.writeBloomUrl(url.getUrl(),
								Constant.BLOOM_FILTER_FILE);
					}
				} else {

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return count;
	}
	*/

	/**
	 * 判断bloom里是否包含传给的字符串，不包含则插入布隆文件和内存
	 * @param url
	 * @return
	 */
	public synchronized boolean containUrl(String url){
		if(url == null){
			return true;
		}
		
		if(!filter.contains(url)){
			//写入内存
			filter.insert(url);
			//写入布隆文件
			filter.writeBloomUrl(url, Constant.PATH.BLOOM_IDR_VIDEO);
			
			return false;
		}
		return true;
	}

}
