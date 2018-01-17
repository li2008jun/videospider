package com.aioff.spider.videospider.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * <p/>
 * Created by zhengjunjun on 2017/4/12.
 */
public class FileUtil {

    public static void checkAndMakeDirecotry(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 根据文件名称获取文件(没有文件则创建个新的文件）
     * @param path
     * @param name
     * @return
     * @throws IOException
     */
    public static File getFile(String path, String name) throws IOException {
        checkAndMakeDirecotry(path);
        File file = new File(path, name);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static String[] FORMAT = new String[] { "cur", "jpg", "jpeg", "png","ico",
            "gif", "bmp" };

    public static boolean  isImageUrl(String url){
        if(url.contains("image")) return true;
        for(String format: FORMAT){
            if(url.endsWith("."+format)){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件后缀名称,默认为jpg格式
     */
    public static String getImageSuffix(String name) {
        if (name == null)
            throw new IllegalArgumentException(
                    "illegal image name , cannot be null!");
        name = name.toLowerCase();
        for (String str : FORMAT) {
            if (name.endsWith(str)) {
                return str;
            }
        }
        return "jpg";
    }


    public static String[] FORMAT_VIDEO = new String[] { ".mp4", ".flv", ".avi", ".rmvb",".rm",
            ".asf", ".divx", ".mpg", ".mpeg", ".wmv", ".mkv", ".vob" };
    /**
     * 获取文件后缀名称,默认为mp4格式
     */
    public static String getVideoSuffix(String url) {
        if (StringUtils.isBlank(url))
            return null;
        url = url.toLowerCase();
        String temp = null;
        if (url.contains("?")) {
            temp = url.substring(url.lastIndexOf("."), url.lastIndexOf("?"));//截取掉某些videoUrl中后带参数的值
        } else {
            temp = url.substring(url.lastIndexOf("."));
        }
        for (String str : FORMAT_VIDEO) {
            if (temp.endsWith(str)) {
                return str.substring(1);
            }
        }
        return "mp4";
    }


    /**
     * 测试用
     * @param args
     */
    public static void main(String[] args) {
        String url = "http://gslb.miaopai.com/stream/N0fl8yQ3umYUAKNqyI3s8mudrkJMkirx.mp4?vend=miaopai&";
        System.out.println(getVideoSuffix(url));
    }
}
