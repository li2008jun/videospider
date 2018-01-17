package com.aioff.spider.videospider.util;

import com.aioff.spider.Constant;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * 工具类
 */
public class Common {
	
	public static  String replaceStr(String src) {
		if (src == null || "".equals(src))
			return null;
		src = src.replaceAll("&lt;", "<");
		src = src.replaceAll("&gt;", ">");
		src = src.replaceAll("&quot;", "\"");
		src = src.replaceAll("&nbsp;", " ");
		src = src.replaceAll("&amp;", "&");
		return src;
	}
	/**
	 * 
	 * 方法名：getList
	 * 作者：lee
	 * 创建时间：2014年8月18日 下午8:15:32
	 * 描述：读取文件内容封装为集合
	 * @param path 文件路径
	 * 默认utf-8编码
	 * @return 封装后的集合
	 */
	public static ArrayList<String> getList(String path){
		return getList(path, Constant.CHARSET_UTF8);
	}
	
	public static String getHost(String url){
		try {
			URL u  = new URL(url);
			return u.getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	
	}
	/**
	 * 
	 * 方法名：getList
	 * 作者：lee
	 * 创建时间：2014年8月18日 下午8:15:32
	 * 描述：读取文件内容封装为集合
	 * @param path 文件路径
	 * @param encoding 文件编码
	 * @return 封装后的集合
	 */
	public static ArrayList<String> getList(String path,String encoding){
		ArrayList<String> result = new ArrayList<String>();
		File file = new File(path);
		FileInputStream fis = null;
		BufferedReader br = null;
		try{
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis,encoding));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();
				if(line.startsWith("#") || line.equalsIgnoreCase("")) continue;
				result.add(line);
			}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeStream(fis, null, br, null);
		}
		return result;
	}
	
	/**
	 * 
	 * 方法名：getSet
	 * 作者：lee
	 * 创建时间：2014年9月12日 下午5:45:54
	 * 描述：读取文件内容封装为set集合
	 * @param path 需要读取的文件路径
	 * @return 封装后的集合
	 */
	public static HashSet<String> getSet(String path){
		HashSet<String> result = new HashSet<String>();
		File file = new File(path);
		FileInputStream fis = null;
		BufferedReader br = null;
		try{
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis,Constant.CHARSET_UTF8));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();
				if(line.startsWith("#") || line.equalsIgnoreCase("")) continue;
				result.add(line);
			}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeStream(fis, null, br, null);
		}
		return result;
	}
	
	/**
	 * 
	 * 方法名：readFileByLineForMap
	 * 作者：lee
	 * 创建时间：2014年9月13日 上午11:57:08
	 * 描述：读取文件内容封装为map集合
	 * @param path
	 * @return
	 */
	public static HashMap<String, Double> getMap(String path){
		
		HashMap<String, Double> result = new HashMap<String, Double>();
		File file = new File(path);
		FileInputStream fis = null;
		BufferedReader br = null;
		try{
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis,Constant.CHARSET_UTF8));
			String line = null;
			String[] array = null;
			while((line = br.readLine()) != null){
				line = line.trim();
				if(line.startsWith("#") || line.equalsIgnoreCase("")) continue;
				array = line.split("=");
				if (array != null && array.length ==2) {
					result.put(array[0], Double.parseDouble(array[1]));
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeStream(fis, null, br, null);
		}
		return result;
	}
	
	/**
	 * 
	 * 方法名：writeFile
	 * 作者：lee
	 * 创建时间：2014年8月21日 上午11:49:23
	 * 描述：将content内容写到文件中
	 * @param filePath 输出文件路径
	 * @param content 要输出的内容(如果不指定则使用UTF-8编码)
	 */
	public static void writeFile(String filePath, String content){
		writeFile(filePath, content, Constant.CHARSET_UTF8);
	}
	
	/**
	 * 
	 * 方法名：writeFile
	 * 作者：lee
	 * 创建时间：2014年8月21日 上午11:48:42
	 * 描述：将content内容写到文件中
	 * @param filePath 输出文件路径
	 * @param content 要输出的内容
	 * @param encoding 字符编码
	 */
	public static void writeFile(String filePath, String content ,String encoding){
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try{
			fos = new FileOutputStream(filePath,true);
			osw = new OutputStreamWriter(fos,encoding);
			content = content + "\r\n";
			osw.write(content);
			osw.flush();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			closeStream(null, fos, null, osw);
		}
	}
	
	/**
	 * 
	 * 方法名：writeFile
	 * 作者：lee
	 * 创建时间：2014年8月21日 上午11:48:42
	 * 描述：将content内容写到文件中
	 * @param filePath 输出文件路径
	 * @param content 要输出的内容
	 * @param encoding 字符编码
	 * @param cover 是否覆盖
	 */
	public static void writeFile(String filePath, String content ,String encoding, boolean cover){
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try{
			fos = new FileOutputStream(filePath,cover);
			osw = new OutputStreamWriter(fos,encoding);
			content = content + "\r\n";
			osw.write(content);
			osw.flush();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			closeStream(null, fos, null, osw);
		}
	}
	
	
	/**
	 * 
	 * 方法名：closeStream
	 * 作者：lee
	 * 创建时间：2014年8月18日 下午8:15:52
	 * 描述：关闭打开的流对象
	 * @param is 可以是一切实现了InputStream接口的对象
	 * @param os 可以是一切实现了OutputStream接口的对象
	 * @param reader 可以是一切实现了Reader抽象类的对象
	 * @param writer 可以是一切实现了Writer抽象类的对象
	 */
	public static void closeStream(InputStream is, OutputStream os, Reader reader, Writer writer){
		try {
			if (is != null) {
				is.close();
			}
			if (os != null){
				os.close();
			}
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
   
	/**
	 * 
	 * 方法名：getText
	 * 作者：lee
	 * 创建时间：2014年9月28日 下午2:35:44
	 * 描述：读取文件内容封装到String中
	 * @param path
	 * @return 返回封装后的String对象
	 */
	public static String getText(String path){
		
		if(path == null || "".equals(path)) return null;
		InputStream is = null;
		BufferedReader reader = null;
		try {
			is = new FileInputStream(new File(path));
			reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null){
				builder.append(line);
				
				
			}
			return builder.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			closeStream(is, null, reader, null);
		}
		
		return null;
	}


	/**
	 * 根据系统返回绝对路径
	 * @param path
	 * @return
	 */
	 public static String getRealPath(String path){
		 return Constant.BASE_DIR + path;
	 }

	/**
	 * 根据系统返回动态下载路径
	 * @return
	 */
	 public static String getDynamicDriver(){
		 return Constant.PATH_DRIVER;
	 }
	/**
	 * 
	 * 方法名：cleanObj
	 * 作者：lee
	 * 创建时间：2014年8月18日 下午8:16:16
	 * 描述：手动将对象置空，便于GC回收内存
	 * @param objs 可变长参数数组
	 */
	public static void cleanObj(Object ...objs){
		for (int i = 0; i < objs.length; i++) {
			objs[i] = null;
		}
	}
	/**
	 * 
	 * 方法名：isNumeric
	 * 作者：lq
	 * 创建时间：2014年10月15日 下午2:57:53
	 * 描述：判断字符串是否为纯数字组成
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str){ 
	    Pattern pattern = Pattern.compile("[0-9]*"); 
	    return pattern.matcher(str).matches();    
	 } 

	/**
	 * 将源码中的Unicode编码转换成utf-8
	 * @param theString
	 * @return
	 */
	public static String decodeUnicode(String theString) {
		if(theString == null) return null;
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';

					else if (aChar == 'n')

						aChar = '\n';

					else if (aChar == 'f')

						aChar = '\f';

					outBuffer.append(aChar);

				}

			} else

				outBuffer.append(aChar);

		}

		return outBuffer.toString();

	}


	/**
	 * 
	 * 方法名：hexToString 
	 * 作者：lee 
	 * 创建时间：2014年12月18日 上午8:44:15 
	 * 描述：将十六进制编码转换为字符串
	 * 
	 * @param content 需要转换的十六进制编码
	 * @return 转换后的字符串
	 */
	public static String hexToString(String content) {
		// 先将十六进制的\x替换掉
		content = content.replaceAll("\\\\x", "");
		if ("0x".equals(content.substring(0, 2))) {
			content = content.substring(2);
		}
		// 创建字节数组，长度是字符串长度除以2
		// 因为在将字符串转换为十六进制编码的时候是先将字符串转成字节数组，再将每一个字节转换为十六进制
		// 相当于字节数组中的每一个字节都对应着长度为2的字符串，（如-21对应E5）
		// 最后结果长度是字节数组长度乘以2，所以在这里还原字节数组的时候需要将字符串长度除以2
		byte[] b = new byte[content.length() / 2];
		for (int i = 0; i < b.length; i++) {
			try {
				// 每移动两位取出一个十六进制数，跟0xFF进行&操作，再将操作的结果转换为十进制字节，存放到对应的位置上
				b[i] = (byte) (0xFF & Integer.parseInt(
						content.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			content = new String(b, "UTF-8");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return content;
	}

	/**
	 * 
	 * 方法名：stringToHex 
	 * 作者：lee 
	 * 创建时间：2014年12月18日 上午8:40:28 
	 * 描述：字符串转换十六进制
	 * 
	 * @param content 需要转换的字符串
	 * @return 转换后的结果
	 */
	public static String stringToHex(String content) {
		StringBuilder builder = new StringBuilder();
		// 首先将字符转换成字节数组
		byte[] b = content.getBytes();
		for (int i = 0; i < b.length; i++) {
			// 取出字节数组中的每一个元素进行，和0xFF进行&操作运算（注意负数需要取反加1），&操作需要将两个数都转换为二进制，同1得1，有0得0，再将位操作的结果转换为十六进制数
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			builder.append("\\x" + hex.toUpperCase());
		}

		return builder.toString();
	}
	/**
	 * 
	 * 方法作用:判断二个地址是否属于同一个网站。
	 * <p>
	 * 
	 * @author liqiang, 2015年4月9日
	 * @param url1
	 * @param url2
	 * @return
	 */
	public static boolean isSameHost(String url1, String url2) {
		String host1 = Common.getHost(url1);
		String host2 = Common.getHost(url2);
		String[] array1 = host1.split("\\.");
		String[] array2 = host2.split("\\.");
		if (array1[array1.length - 1].equals(array2[array2.length - 1])) {
			if (array1[array1.length - 2].equals(array2[array2.length - 2])) {
				return true;
			}
		}
		return false;
	}
	public static String stringFilter(String str){
		if(str == null) return null;
		return str.replaceAll("[xX][fF]{2,4}", "");
	}
	
	
	/**
	 * 根据相对路径和其父级路径获取其绝对路径
	 * @param parentUrl
	 * @param url
	 * @return
	 */
	public static String replenishUrl(String parentUrl,String url){
		
		try {
			return new URL(new URL(parentUrl), url).toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	/**
	 * 
	 * 方法名     :imageZip
	 * 时间         :2015年5月15日 下午7:52:48
	 * 作者         :LiRui
	 * 用途         :压缩图片
	 * 返回类型 :byte[]
	 */
	public static byte[] imageZip(byte[] bytes){
		if(bytes == null){
			return null;
		}
		
		try{
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
			int width = image.getWidth();
			int height = image.getHeight();
					
			double sfBL = 650.0 / (double) 202;
			double realBL = width / (double) height;
			//JSONObject coords = new JSONObject();
			if (realBL > sfBL) {
				// 以高度等比例压缩 剪切2边宽度
				int w = (int) (width * (202 / (double) height));
				int wStart = (int) (width - (270 * width) / (double) w);
				// builder.size(w, MIN_IMAGE_HEIGHT).sourceRegion(wStart / 2, 0,
				// width - wStart, height);
				int x =  wStart / 2;
				int y = 0;
				
				Thumbnails.Builder<?> builder = Thumbnails.of(ImageIO.read(new ByteArrayInputStream(bytes)));
				builder.sourceRegion(x, y,  width - wStart, height);
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				builder.size(w, 202).outputFormat("JPEG").toOutputStream(outStream);
				
				return outStream.toByteArray();
			} else {
				// 以宽度等比例压缩 剪切上下2边高度
				int h = (int) (height * (270 / (double) width));
				int hStart = (int) (height - (202 * height) / (double) h);
				// builder.size(MIN_IMAGE_WIDTH, h).sourceRegion(0, 0, width, height
				// - hStart);
				int x = 0;
				int y = 0;
				
				Thumbnails.Builder<?> builder = Thumbnails.of(ImageIO.read(new ByteArrayInputStream(bytes)));
				builder.sourceRegion(x, y,  width, height - hStart);
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				builder.size(270, h).outputFormat("JPEG").toOutputStream(outStream);
				
				return outStream.toByteArray();
			}
		}catch(Exception e){
			return null;
		}
		
	}
}
