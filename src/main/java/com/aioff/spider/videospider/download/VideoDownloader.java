package com.aioff.spider.videospider.download;

import com.aioff.spider.Constant;
import com.aioff.spider.videospider.util.FileUtil;
import com.aioff.spider.videospider.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 视频下载工具类
 * Created by zhengjunjun on 2017/5/23.
 */
public class VideoDownloader {
    private static Logger logger = LoggerFactory.getLogger(VideoDownloader.class);

    private CloseableHttpClient httpClient = null;

    // 视频下载目录
    private static String path = Constant.BASE_DIR + "conf/video";

    public static VideoDownloader getInstance() {
        return new VideoDownloader();
    }

    /**
     * 过滤特殊字符
     */
    public String StringFilter(String str) throws PatternSyntaxException {
        // 只允许字母和数字  String regEx="[^a-zA-Z0-9]";
        // 清除掉所有特殊字符  不能为文件名字符主要有  \ / : * ? " < > →
        String regEx = "[`~!@#$%^&*()+=|{}':：;,\\[\\].<>→/?！￥…（）—【】‘；”“’。，、？\" ]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 获取CloseableHttpClient对象，无则生成
     * @return
     */
    protected synchronized CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = generateClient();
        }
        return httpClient;
    }

    /**
     * 配置信息，生成CloseableHttpClient
     * @return
     */
    private CloseableHttpClient generateClient() {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(reg);
        connectionManager.setDefaultMaxPerRoute(100);
        //connectionManager.setMaxTotal(threadNum);
        HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connectionManager);
        builder.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36");
        // 尽量采用压缩格式传输，节约宽带
        builder.addInterceptorFirst(new HttpRequestInterceptor() {
            public void process(HttpRequest httpRequest, org.apache.http.protocol.HttpContext httpContext) throws HttpException, IOException {
                if (httpRequest.containsHeader("Accept-Encoding")) {
                    httpRequest.addHeader("Accept-Encoding", "gzip");
                }
            }
        });
        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setTcpNoDelay(true).build();
        builder.setDefaultSocketConfig(socketConfig);
        builder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, true));

        CookieStore cookieStore = new BasicCookieStore();
        builder.setDefaultCookieStore(cookieStore);

        return builder.build();
    }

    /**
     * 删除本地视频
     * @param file_path
     */
    public static void deleteFile(String file_path) {
        if (StringUtils.isBlank(file_path)) {
            return;
        }

        // 删除本地图片
        try {
            File file = new File(file_path);
            if (!file.exists()) {
                return;
            }

            if (file.delete()) {
                logger.info("删除本地视频成功！" + file_path);
            } else {
                logger.info("删除本地视频失败！" + file_path);
            }
        } catch (Exception e) {
            logger.error("删除本地视频时出错！" + file_path, e);
        }
    }


    /**
     * 下载视频
     * @author zjj, 2017-05-23
     * @param videoUrl
     * @param videoId
     * @return 视频本地地址
     */
    public String downloadVideo(String videoUrl, String videoId) {
        if (StringUtils.isBlank(videoUrl) || StringUtils.isBlank(videoId)) {
            return null;
        }

        long beginTime = System.currentTimeMillis();

        // 获取.mp4后缀
        String suffix = FileUtil.getVideoSuffix(videoUrl);

        InputStream in = null;
        OutputStream out = null;
        File temp = null;

        HttpUriRequest get = null;
        CloseableHttpResponse response = null;

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectionRequestTimeout(5000);
        requestConfigBuilder.setSocketTimeout(10000);
        requestConfigBuilder.setConnectTimeout(5000);
        requestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);

        try {
            get = RequestBuilder.get().setUri(videoUrl).setConfig(requestConfigBuilder.build())
                    .addHeader("Host", UrlUtil.getHost(videoUrl))
                    .build();
            response = getHttpClient().execute(get);
            int httpCode = response.getStatusLine().getStatusCode();
            if (httpCode != HttpStatus.SC_OK) {
                logger.info("视频下载失败！videoUrl = " + videoUrl);
                return null;
            }

            String videoName = videoId + "." + suffix;
            String videoPath = path + "/" + videoName;
            // 删除同名视频
            deleteFile(videoPath);

            temp = FileUtil.getFile(path, videoName);
            out = new FileOutputStream(temp);
            in = response.getEntity().getContent();

            int len = 0;
            byte[] bytes = new byte[4096];
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);

                if ((System.currentTimeMillis() - beginTime) / 1000 > 60 * 55) {
                    logger.info("网络下载超过5分钟，丢弃该视频，url = " + videoUrl);
                    //删mongo
                    //collection.deleteOne(new Document("_id", videoInfo.get_id()));
                    /*collection.updateOne(new Document("_id", videoInfo.get_id()),
                            new Document("$set", new Document("status", 3).append("tips", "下载超时/失败")));*/
                    logger.info("***************************************************************************");
                    logger.info("$$$$$$ 视频下载超时/失败-丢弃: " + videoUrl);
                    logger.info("***************************************************************************");
                    return null;
                }
            }

            logger.info("【视频下载成功，耗时: {} 秒, videoPath: {}, videoUrl: {}】",
                    (System.currentTimeMillis() - beginTime) / 1000, videoPath, videoUrl);
            return videoPath;
        } catch (Exception e) {
            logger.error("【视频下载异常：" + videoUrl, e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }

    /**
     * 测试用
     * @param args
     */
    public static void main(String[] args) {
        String videoUrl = "http://v3.365yg.com/35159e24500609be24c06d869e42f504/5964c486/video/m/220447740a394044b818f66159ff800b7081148c62000044a898fecfae/";
        String videoId = "2";
        String path = getInstance().downloadVideo(videoUrl, videoId);
        System.out.println(path);
    }

}
