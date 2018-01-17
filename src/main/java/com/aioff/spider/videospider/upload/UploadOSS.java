package com.aioff.spider.videospider.upload;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 上传图片到OSS
 * Created by zhengjunjun on 2017/4/14.
 */
public class UploadOSS {
    private static Logger logger = LoggerFactory.getLogger(UploadOSS.class);

    private static String endpoint = "http://oss-cn-hongkong.aliyuncs.com";
    private static String accessKeyId = "LTAIQxL1QMH6ktfs";
    private static String accessKeySecret = "3oK7mhE3gQa5xxg4xBT6DvtASoJVd4";
    private static String bucketName = "videotoday";
    private static String key = "<downloadKey>";
    private static String uploadFile = "<uploadFile>";

    /** 上传图片url前缀 */
    private static String uploadUrl_prefix = "http://videotoday.oss-cn-hongkong.aliyuncs.com/video/";

    /**
     * 上传到OSS服务器
     * @param filePath
     * @return
     */
    public static String uploadImage(String filePath) {
        String uploadUrl = null;

        File file = new File(filePath);
        String name = file.getName();
        String Objectkey = "video/" + name;

        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId,accessKeySecret);

        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(file.length());
        //判断上传类型，多的可根据自己需求来判定
        if (filePath.endsWith("xml")) {
            objectMeta.setContentType("text/xml");
        }
        else if (filePath.endsWith("jpg")) {
            objectMeta.setContentType("image/jpeg");
        }
        else if (filePath.endsWith("png")) {
            objectMeta.setContentType("image/png");
        }

        InputStream input;
        try {
            input = new FileInputStream(file);
            ossClient.putObject(bucketName, Objectkey, input, objectMeta);
            uploadUrl = uploadUrl_prefix + name;
        } catch (Exception e) {
            logger.error("上传图片时出错 " + filePath, e);
        }

        return uploadUrl;
    }

    /**
     * 上传到OSS服务器
     * @param filePath
     * @return
     */
    public static String uploadVideo(String filePath) {
        String uploadUrl = null;

        File file = new File(filePath);
        String name = file.getName();
        String Objectkey = "video/" + name;

        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId,accessKeySecret);

        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(file.length());
        //判断上传类型，多的可根据自己需求来判定
        if (filePath.endsWith("mp4")) {
            objectMeta.setContentType("video/mpeg4");
        }

        InputStream input;
        try {
            input = new FileInputStream(file);
            PutObjectResult result = ossClient.putObject(bucketName, Objectkey, input, objectMeta);
            uploadUrl = uploadUrl_prefix + name;
        } catch (Exception e) {
            logger.error("上传图片时出错 " + filePath, e);
        }

        return uploadUrl;
    }

    /**
     * 删除单个文件
     * @param name - 文件名
     * @return
     */
    public static boolean delete(String name) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId,accessKeySecret);
        String Objectkey = "video/" + name;

        try {
            // 删除Object
            ossClient.deleteObject(bucketName, Objectkey);
            return true;
        } catch (Exception e) {
            logger.error("删除时出错 " + key, e);
        }

        // 关闭client
        ossClient.shutdown();

        return false;
    }



    public static void main(String[] args) {
        String filePath = "E:\\conf\\video\\1.mp4";
        String uploadUrl = uploadVideo(filePath);
        System.out.print(uploadUrl);
       // delete("2.mp4");
    }
}
