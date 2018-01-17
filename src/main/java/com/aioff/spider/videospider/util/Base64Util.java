package com.aioff.spider.videospider.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Base64编码、解码
 * Created by zhengjunjun on 2017/5/21.
 */
public class Base64Util {
    private static Logger logger = LoggerFactory.getLogger(Base64Util.class);

    /**
     * Base64编码
     * @param account
     * @return
     */
    public static String encodeBase64(String account) {
        String result = "";
        try {
            result = new String(Base64.encodeBase64(URLEncoder.encode(account, "UTF-8").getBytes()));
            //StringUtils.base64Encode(URLEncoder.encode(account, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encodeBase64() occur error!", e);
        }
        return result;
    }

    /**
     * Base64解码
     * @param account
     * @return
     */
    public static String decodeBase64(String account) {
        String result = "";
        try {
            result = new String(Base64.decodeBase64(URLDecoder.decode(account, "UTF-8").getBytes()));
            //StringUtils.base64Encode(URLEncoder.encode(account, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encodeBase64() occur error!", e);
        }
        return result;
    }
}
