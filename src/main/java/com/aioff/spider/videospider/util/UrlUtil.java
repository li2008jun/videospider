package com.aioff.spider.videospider.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * <p/>
 * Created by zhengjunjun on 2017/4/12.
 */
public class UrlUtil {

    public static String getHost(String url) {
        try {
            URL u = new URL(url);
            return u.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String canonicalizeUrl(String url, String baseUrl) {
        if(StringUtil.isBlank(url)) return null;
        try {
            return new URL(new URL(baseUrl), url).toString();
        } catch (MalformedURLException e) {
//			e.printStackTrace();
        }
        return url;
    }
    
    public static String transformUrl(String url) {
    	if(StringUtils.isEmpty(url)){
    		 return url;
    	}
    	String regex = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?";
    	boolean isMatch = Pattern.matches(regex, url);
    	if(!isMatch){
    		if(url.startsWith("//")){
    			url = "http:" + url;
    		}else if(url.startsWith("/")){
    			url = "http:/" + url;
    		}else{
    			url = "http://" + url;
    		}
    	}
        return url;
    }

    /**
     * 测试用
     * @param args
     */
    public static void main(String[] args) throws Exception{
        String imageUrl = "//p1.pstatp.com/list/300x170/3952000ebfd1173d9480";
        System.out.println(getHost(imageUrl));
    }
}
