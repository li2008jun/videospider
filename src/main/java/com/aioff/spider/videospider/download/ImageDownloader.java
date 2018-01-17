package com.aioff.spider.videospider.download;

import com.aioff.spider.Constant;
import com.aioff.spider.util.Sign;
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
import org.apache.http.cookie.SM;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片下载
 * Created by zhengjunjun on 2017/5/19.
 */
public class ImageDownloader {
    private static Logger logger = LoggerFactory.getLogger(ImageDownloader.class);

    private CloseableHttpClient httpClient = null;

    protected boolean dynamicProxy = true;

    public static ImageDownloader getInstance() {
        return new ImageDownloader();
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
     * 获取Cookie
     * @param response
     * @param httpResponse
     */
    private void getCookie(Response response, HttpResponse httpResponse) {
        HeaderIterator hit = httpResponse.headerIterator(SM.SET_COOKIE);

        while (hit.hasNext()) {
            Header header = hit.nextHeader();
            HeaderElement[] headerElement = header.getElements();

            if (headerElement == null) {
                return;
            }

            for (HeaderElement he : headerElement) {
                if (he.getName() == null || he.getValue() == null) {
                    continue;
                }

                response.addCookie(he.getName(), he.getValue());
            }
        }
    }

    /**
     * 下载图片到本地 - 用代理和cookie
     * @author zjj, 2017-05-19
     * @param imageUrl
     * @param imageName
     * @param proxy
     * @param cookie
     * @return - filePath,response
     */
    public Map<String, Object> downloadImage_response(String imageUrl, String imageName, HttpHost proxy, String cookie) {
        if (StringUtils.isBlank(imageUrl) || StringUtils.isBlank(imageName)) {
            return null;
        }
        String filePath = null;
        Map<String, Object> responseMap = new HashMap<String, Object>();

        InputStream in = null;
        OutputStream out = null;
        File temp = null;

        HttpUriRequest get = null;
        CloseableHttpResponse response = null;
        Response response_cookie = new Response();

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectionRequestTimeout(5000);
        requestConfigBuilder.setSocketTimeout(15000);
        requestConfigBuilder.setConnectTimeout(5000);
        requestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);
        if (proxy != null) {
            requestConfigBuilder.setProxy(proxy);
        }

        try {
            get = RequestBuilder.get().setUri(imageUrl).setConfig(requestConfigBuilder.build())
                    .addHeader("Host", UrlUtil.getHost(imageUrl))
                    .addHeader("Cookie", cookie)
                    .build();
            response = getHttpClient().execute(get);
            int httpCode = response.getStatusLine().getStatusCode();
            if (httpCode != HttpStatus.SC_OK) {
                logger.info("图片下载失败！imageUrl = " + imageUrl);
                return null;
            }

            temp = FileUtil.getFile(Constant.PATH.IMAGE, imageName);
            out = new FileOutputStream(temp);
            in = response.getEntity().getContent();
            /***************************/
            Header[] headers = response.getAllHeaders();
            getCookie(response_cookie, response);
            /*******************************/

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }

            filePath = Constant.PATH.IMAGE + imageName;
            logger.info("图片下载成功！imageName[{}], filePath[{}], imageUrl[{}]", imageName, filePath, imageUrl);
            responseMap.put("filePath", filePath);
            responseMap.put("response", response_cookie);
            return responseMap;
        } catch (Exception e) {
            logger.error("下载图片时出错！imageUrl = " + imageUrl, e);
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

        return responseMap;
    }

    /**
     * 普通下载图片到本地，返回本地路径
     * @author zjj, 2017-05-19
     * @param imageUrl
     * @param imageName
     * @param pageUrl
     * @return
     */
    public String downloadImage(String imageUrl, String imageName, String pageUrl) {
        if (StringUtils.isBlank(imageUrl) || StringUtils.isBlank(imageName)
                || StringUtils.isBlank(pageUrl)) {
            return null;
        }

        String filePath = null;

        InputStream in = null;
        OutputStream out = null;
        File temp = null;

        HttpUriRequest get = null;
        CloseableHttpResponse response = null;
        Response response_cookie = new Response();

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectionRequestTimeout(5000);
        requestConfigBuilder.setSocketTimeout(15000);
        requestConfigBuilder.setConnectTimeout(5000);
        requestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);

        try {
            get = RequestBuilder.get().setUri(imageUrl).setConfig(requestConfigBuilder.build())
                    .addHeader("Host", UrlUtil.getHost(imageUrl))
                    .build();
            // 微信图片不设置Referer
            if (!imageUrl.contains("mmbiz")) {
                get.setHeader("Referer", pageUrl);
            }

            response = getHttpClient().execute(get);
            int httpCode = response.getStatusLine().getStatusCode();
            if (httpCode != HttpStatus.SC_OK) {
                logger.info("图片下载失败！imageUrl = " + imageUrl);
                return null;
            }

            filePath = Constant.PATH.IMAGE + "/"+ imageName;
            // 删除同名图片
            deleteImage(filePath);

            temp = FileUtil.getFile(Constant.PATH.IMAGE, imageName);
            out = new FileOutputStream(temp);
            in = response.getEntity().getContent();

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }

            logger.info("图片下载成功！imageName[{}], filePath[{}], imageUrl[{}]", imageName, filePath, imageUrl);
            return filePath;
        } catch (Exception e) {
            logger.error("下载图片时出错！imageUrl = " + imageUrl, e);
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
     * 删除本地图片
     * @param image_file_path
     */
    public void deleteImage(String image_file_path) {
        if (StringUtils.isBlank(image_file_path)) {
            return;
        }

        // 删除本地图片
        try {
            File image_file = new File(image_file_path);
            if (!image_file.exists()) {
                return;
            }

            if (image_file.delete()) {
                logger.info("删除本地图片成功！image_path=" + image_file_path);
            } else {
                logger.info("删除本地图片失败！image_path=" + image_file_path);
            }
        } catch (Exception e) {
            logger.error("删除本地图片时出错！image_path=" + image_file_path, e);
        }
    }


    /**
     * 测试用
     * @param args
     */
    public static void main(String[] args) {
        String imageUrl = "http://weixin.sogou.com/antispider/util/seccode.php?tc=1492584830";
        String imageName = Sign.getMD5(imageUrl) + "." + FileUtil.getImageSuffix(imageUrl);
        String pageUrl = "http://weixin.sogou.com/";

        String filePath = ImageDownloader.getInstance().downloadImage(imageUrl, imageName, pageUrl);

        System.out.println("the end");
    }
}
