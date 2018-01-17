package com.aioff.spider.videospider.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抽取工具类
 * Created by zjj on 2017/05/04.
 */
public class JsoupUtils {
    private static Logger logger = LoggerFactory.getLogger(JsoupUtils.class);

    /**
     * 从文本中提取数字
     * @author zjj, 2017-05-20
     * @param str
     * @return
     */
    public static double extractDouble(String str) {
        String num_str = null;
        String regex = "(-?\\d+\\.?\\d*)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            num_str = matcher.group(0);
        }

        if (StringUtils.isNotBlank(num_str)) {
            return Double.parseDouble(num_str);
        } else {
            return 0.0;
        }
    }

    /**
     * "2.1万"-》21000
     * @author zjj, 2017-05-22
     * @param count
     * @return
     */
    public static long getNum(String count) {
        if (StringUtils.isNotBlank(count)) {
            double count_init = JsoupUtils.extractDouble(count);
            long num = 0L;
            if (count.contains("万")) {
                Double s = count_init * 10000;
                num = new Double(s).longValue();
            } else {
                num = new Double(count_init).longValue();
            }
            return num;
        }
        return 0L;
    }

    /**
     * 正则匹配获取结果
     * @author zjj, 2017-05-18
     * @param str
     * @param regex
     * @return
     */
    public static String regexText(String str, String regex) {
        if (StringUtils.isBlank(str) || StringUtils.isBlank(regex)) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(0);
        }

        return null;
    }

    /**
     * 根据模板抽取文本
     * @param doc
     * @param tags
     * @param type
     * @return
     */
    public static String extractText(Element doc, String tags, int type) {
        if (doc == null || StringUtils.isBlank(tags)) {
            return null;
        }

        String result = null;
        String[] array = tags.split("\\$");
        for (String tag : array) {
            if (StringUtils.isNotBlank(tag)) {
                if (tag.startsWith("r@")) {
                    //此规则是正则表达式
                    String regex = tag.substring(2);
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(doc.html());
                    if (matcher.find()) {
                        result = matcher.group(0);
                    }
                } else {
                    //此规则为定位标签
                    try {
                        String[] arr = tag.split(" ");
                        if (arr.length < 1) {
                            continue;
                        } else if (arr.length > 1) {
                            tag = arr[0];
                        }

                        Elements elements = doc.select(tag);
                        if (elements == null || elements.size() == 0) {
                            continue;
                        }
                        Element need_ele = elements.get(0);
                        if (arr.length > 1) {
                            result = need_ele.attr(arr[1]);
                        } else {
                            switch (type) {
                                case 2: result = need_ele.html();
                                    break;
                                case 3: result = need_ele.outerHtml();
                                    break;
                                case 4: result = need_ele.ownText();
                                    break;
                                default: result = need_ele.text();
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            //抽取到信息，及时break
            if (StringUtils.isNotBlank(result)) {
                break;
            }
        }

        if (result != null) {
            return result.trim();
        } else {
            return null;
        }
    }

    /**
     * "01:35"之类转化为秒数
     * @author zjj, 2017-05-22
     * @param str
     * @return
     */
    public static long getDuration(String str) {
        long duration = 0L;
        if (StringUtils.isBlank(str)) {
            return 0L;
        }

        try {
            String[] arr = str.split(":");
            if (arr.length == 1) {
                duration = Long.parseLong(arr[0]);
            } else if (arr.length == 2) {
                long minute = Long.parseLong(arr[0]);
                long second = Long.parseLong(arr[1]);
                duration = minute * 60 + second;
            } else if (arr.length == 3) {
                long hour = Long.parseLong(arr[0]);
                long minute = Long.parseLong(arr[1]);
                long second = Long.parseLong(arr[2]);
                duration = hour * 3600 + minute * 60 + second;
            }
        } catch (Exception e) {
            logger.error("转化时长异常：" + str, e);
        }

        return duration;
    }

}
