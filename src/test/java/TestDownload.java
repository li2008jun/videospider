import com.aioff.spider.downloader.HttpClientDownloader;
import com.aioff.spider.downloader.SeleniumDownloader;
import com.aioff.spider.entity.Page;
import com.aioff.spider.entity.Request;
import com.aioff.spider.util.Common;

/**
 * Created by zhengjunjun on 2016/4/15 0015.
 */
public class TestDownload {

    public static void main(String[] args) {
        //System.setProperty("spider.env","local");
        String url = "http://v1.365yg.com/a3ad856e9086779717b2f8c5c2cfde2e/5964c583/video/m/220447740a394044b818f66159ff800b7081148c62000044a898fecfae/";
        int dynamic = 0;
        Page page = null;
        String html = null;
        try {
            if (dynamic == 0) {
                page = HttpClientDownloader.DEFAULT().download(new Request(url));
            } else {
                page =new SeleniumDownloader().downloadSleep1s(new Request(url));
            }
        } catch (Exception e) {

        }
        html = page.getRawText();
        System.out.println(html);
        Common.writeFile("D:/test.txt", html, "UTF-8", false);
        System.out.println("the end");

    }


}
