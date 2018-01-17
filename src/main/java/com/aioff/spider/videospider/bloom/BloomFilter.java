package com.aioff.spider.videospider.bloom;


import com.aioff.spider.Constant;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;

import com.aioff.spider.videospider.util.Common;

/**
 * 布隆过滤器工具类
 */
public class BloomFilter {

	private static BloomFilter filter = new BloomFilter();

	private BloomFilter() {
		init();
	}
	
	public static BloomFilter getInstance(){
		return filter;
	}
	
	// DEFAULT_SIZE为2的25次方
	private final int DEFAULT_SIZE = 2 << 28;
	/** 不同哈希函数的种子，一般应取质数,seeds数据共有7个值，则代表采用8种不同的HASH算法 */
	private final int[] seeds = new int[] { 5, 7, 11, 13, 31, 37, 61 };

	// BitSet实际是由“二进制位”构成的一个Vector。假如希望高效率地保存大量“开－关”信息，就应使用BitSet.
	// BitSet的最小长度是一个长整数（Long）的长度：64位
	private BitSet bits = new BitSet(DEFAULT_SIZE);

	/* 哈希函数对象 */
	private SimpleHash[] func = new SimpleHash[seeds.length];

	/**
	 * 哈希函数类
	 */
	public static class SimpleHash {
		// cap为DEFAULT_SIZE的值，即用于结果的最大的字符串长度。
		// seed为计算hash值的一个给定key，具体对应上面定义的seeds数组
		private int cap;
		private int seed;

		public SimpleHash(int cap, int seed) {
			this.cap = cap;
			this.seed = seed;
		}

		public int hash(String value) {
			int result = 0;
			int len = value.length();
			for (int i = 0; i < len; i++) {
				result = seed * result + value.charAt(i);
			}
			return (cap - 1) & result;
		}
	}

	/**
	 * 将字符串标记到bits中，即设置字符串的8个hash值函数为1
	 * @param value
	 */
	public synchronized void insert(String value) {
		for (SimpleHash f : func) {
			bits.set(f.hash(value), true);
		}
	}


	/**
	 * 判断字符串是否已经被bits标记
	 * @param value
	 * @return
	 */
	public synchronized boolean contains(String value) {
		// 确保传入的不是空值
		if (value == null) {
			return false;
		}

		// 计算7种hash算法下各自对应的hash值，并判断
		for (SimpleHash f : func) {
			if (!bits.get(f.hash(value)))
				return false;
		}
		return true;
	}

	/**
	 * 初始化布隆过滤器
	 */
	public void init() {

		for (int i = 0; i < seeds.length; i++) {
			// 给出所有的hash值，共计seeds.length个hash值。共8位。
			// 通过调用SimpleHash.hash(),可以得到根据7种hash函数计算得出的hash值。
			// 传入DEFAULT_SIZE(最终字符串的长度），seeds[i](一个指定的质数)即可得到需要的那个hash值的位置。
			func[i] = new SimpleHash(DEFAULT_SIZE, seeds[i]);
		}

		readUrlFromDir(Constant.PATH.BLOOM_IDR_VIDEO);

	}


	/**
	 * 从目录中读取bloom文件  文件啊名称格式 ：bloom_201401.txt
	 * @param path
	 */
	public void readUrlFromDir(String path){
		File dir = new File(path);
		if(!dir.exists()){
			//布隆目录不存在，将创建
			dir.mkdirs();
		}
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				String fileName = files[i].getName();

				if (fileName.startsWith("bloom")) {
					InputStream is = null;
					BufferedReader br = null;
					try {
						is = new FileInputStream(files[i]);
						br = new BufferedReader(new InputStreamReader(is,
								Constant.CHARSET_UTF8));
						String line = null;
						while ((line = br.readLine()) != null) {
							if (!"".equals(line)) {
								insert(line.trim());
							}
						}
					} catch (FileNotFoundException e) {
//						布隆配置文件未加载到
//						需要记录日志
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally{
						Common.closeStream(is, null, br, null);
					}
				}
			}
		}
	}

	/**
	 * 将url写入布隆文件
	 * @param url
	 * @param path
	 */
	public void writeBloomUrl(String url,String path){
		File dir = new File(path);
		if(!dir.exists()){
//			Logger.debug("布隆过滤器存储目录不存在，将重新建立");
			dir.mkdirs();
		}
		
		String format = "yyyyMM";
		DateFormat df = new SimpleDateFormat(format);
		String time = df.format(new Date());
		String fileName = "bloom_"+time+".txt";
		if(!path.endsWith("\\")&&!path.endsWith("/")){
			path = path +"\\";
		}
		Common.writeFile(path+fileName, url);
		
	}


	public static void main(String[] args) {
		
//		System.out.println(BloomFilter.getInstance().contains("http://www.plating.org/news_info.asp?pid=28&id=2857"));
		new BloomFilter().writeBloomUrl("ddddd", "f:/test");
	}

}
