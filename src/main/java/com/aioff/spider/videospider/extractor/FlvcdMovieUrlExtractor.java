package com.aioff.spider.videospider.extractor;

import com.aioff.spider.downloader.Downloader;
import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 利用硕鼠官网api提取视频地址
 * Created by liqiang on 2015/11/9.
 */
public class FlvcdMovieUrlExtractor {
    private static final Downloader downloader = new HttpClientDownloader();

    private String api = "http://www.flvcd.com/parse.php?format=&kw=";

    /**
     * 利用硕鼠官网api提取视频地址——使用代理
     * @param url
     * @return
     */
    public synchronized List<String> getMovieUrl(String url) {
        Request request = null;
        try {
            Thread.sleep(1000 * 2);
            request = new Request(api + URLEncoder.encode(url, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.addHead("Referer", api + url);
        request.addHead("Host", "www.flvcd.com");

        //下载源码——利用代理
        String src = null;
        String urlApi = api + url;
       // src = InfoLoad.getInstance().loadForString(urlApi, 0).getPageContent();
        src = downloader.download(new Request(urlApi)).getRawText();
        //有时可能因为代理ip的原因源码下载不全，需要做个判断，源码长度小于220的重新下载一次
        if(src != null && src.length()<220){
            src = downloader.download(new Request(urlApi)).getRawText();
        }
        if(src == null ){
            return null;
        }
        Document doc = Jsoup.parse(src);

        //老版——不用代理直接下载
       /* Page page = downloader.download(request);
        if (page.getStatusCode() != 200) {
            System.out.println(url + ":" + page.getStatusCode());
            return null;
        }
        Document doc = Jsoup.parse(page.getRawText());*/

        Elements eles = doc.select("table td");
        if (eles == null || eles.size() == 0) return null;

        List<String> list = new ArrayList<String>();
        for (Element ele : eles) {
            String text = ele.text();
            if (!text.contains("下载地址")) {
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
     * 利用硕鼠官网api提取视频地址——不用代理
     * @author zhengjunjun, 2016-04-14
     * @param url
     * @return
     */
    public synchronized List<String> getMovieUrlNoProxy(String url) {
        Request request = null;
        try {
            Thread.sleep(1000);
            request = new Request(api + URLEncoder.encode(url, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.addHead("Referer", api + url);
        request.addHead("Host", "www.flvcd.com");

        //下载源码，不用代理直接下载
        Page page = downloader.download(request);
        if (page.getStatusCode() != 200) {
            System.out.println(url + ":" + page.getStatusCode());
            return null;
        }
        Document doc = Jsoup.parse(page.getRawText());

        Elements eles = doc.select("table td");
        if (eles == null || eles.size() == 0) return null;

        List<String> list = new ArrayList<String>();
        for (Element ele : eles) {
            String text = ele.text();
            if (!text.contains("下载地址")) {
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
     * 测试 flvcd 视频地址抽取
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("spider.env", "local");
        List<String> urls = new FlvcdMovieUrlExtractor().
                getMovieUrl("http://www.le.com/ptv/vplay/24608803.html#vid=24608803");
        for (String str : urls) {
            System.out.println(str);
        }
    }

}
