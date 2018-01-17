package com.aioff.spider.videospider.extractor;

import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import com.aioff.spider.util.HttpConstant;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 利用http://www.shokdown.com/舒克视频下载提取视频地址
 * Created by zhengjunjun on 2016/4/12 0012.
 */
public class ShokdownMovieExtractor {
    private Logger logger = LoggerFactory.getLogger(ShokdownMovieExtractor.class);

    private static String api = "http://www.shokdown.com/parse.php";

    /**
     * 利用http://www.shokdown.com/舒克视频下载提取视频地址——利用代理
     * @param url
     * @return
     */
    public synchronized List<String> getMovieUrl(String url) {
        // 定义请求
        Request request = null;
        try {
            Thread.sleep(1000 * 6);
            request = new Request(api);
        } catch (Exception e) {
            logger.error("Thread sleep occur error!", e);
        }
        request.addHead("Referer", "http://www.shokdown.com/");
 //       request.addHead("Host", "www.flvcd.com");

        // 请求的方式
        request.setMethod(HttpConstant.Method.POST);
        //创建一个对象数组将解析url设置进去
        NameValuePair[] nameValuePairs = new NameValuePair[1];
        NameValuePair name = new BasicNameValuePair("url", url);
        nameValuePairs[0] = name;
        //发送请求
        request.setExtra("nameValuePair",nameValuePairs);

        //未使用代理
        //Page page = HttpClientDownloader.DEFAULT().download(request);
        // 使用代理
        Page page = HttpClientDownloader.DEFAULT().downloadByProxy(request);
        String src = page.getRawText();
        if (page.getStatusCode() != 200) {
            System.out.println(url + ":" + page.getStatusCode());
            return null;
        }
        Document doc = Jsoup.parse(src);

        //解析出下载地址链接
        Elements eles = doc.select("table td");
        if (eles == null || eles.size() == 0) return null;

        List<String> list = new ArrayList<String>();
        for (Element ele : eles) {
            String text = ele.text();
            if (!text.contains("点击或目标另存为下载")) {
                continue;
            }
            Elements as = ele.select("a");
            if (as == null) continue;
            list.add(as.attr("href"));
        }
        if (list.size() == 0) {
            return null;
        }

        return list;
    }

    /**
     * 利用http://www.shokdown.com/舒克视频下载提取视频地址——不用代理
     * @author zhengjunjun, 2016-04-14
     * @param url
     * @return
     */
    public synchronized List<String> getMovieUrlNoProxy(String url) {
        // 定义请求
        Request request = null;
        try {
            Thread.sleep(1000 * 6);
            request = new Request(api);
        } catch (Exception e) {
            logger.error("Thread sleep occur error!", e);
        }
        request.addHead("Referer", "http://www.shokdown.com/");
        //       request.addHead("Host", "www.flvcd.com");

        // 请求的方式
        request.setMethod(HttpConstant.Method.POST);
        //创建一个对象数组将解析url设置进去
        NameValuePair[] nameValuePairs = new NameValuePair[1];
        NameValuePair name = new BasicNameValuePair("url", url);
        nameValuePairs[0] = name;
        //发送请求
        request.setExtra("nameValuePair",nameValuePairs);

        //未使用代理
        Page page = HttpClientDownloader.DEFAULT().download(request);
        String src = page.getRawText();
        if (page.getStatusCode() != 200) {
            System.out.println(url + ":" + page.getStatusCode());
            return null;
        }
        Document doc = Jsoup.parse(src);

        //解析出下载地址链接
        Elements eles = doc.select("table td");
        if (eles == null || eles.size() == 0) return null;

        List<String> list = new ArrayList<String>();
        for (Element ele : eles) {
            String text = ele.text();
            if (!text.contains("点击或目标另存为下载")) {
                continue;
            }
            Elements as = ele.select("a");
            if (as == null) continue;
            list.add(as.attr("href"));
        }
        if (list.size() == 0) {
            return null;
        }

        return list;
    }

    /**
     * 测试 舒克 视频地址抽取
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("spider.env", "local");
        List<String> urls = new ShokdownMovieExtractor().
                getMovieUrlNoProxy("http://v.163.com/paike/VBOP45426/VHJVSH9P1.html");
        for (String str : urls) {
            System.out.println(str);
        }
    }





}
