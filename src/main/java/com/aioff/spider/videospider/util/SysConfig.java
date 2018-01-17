package com.aioff.spider.videospider.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SysConfig {

	private static Logger logger = LoggerFactory.getLogger(SysConfig.class);

	public static final String CONFIG_FILE = "/spider.properties";

	private static SysConfig instance = new SysConfig();

	private static Properties props = new Properties();

	static {
		InputStream in = SysConfig.class.getResourceAsStream(CONFIG_FILE);

		try {
			props.load(new InputStreamReader(in, "UTF-8"));
		} catch (IOException e) {
			logger.error("加载配置文件失败[{}]", CONFIG_FILE, e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private SysConfig() {
	}

	public static SysConfig getInstance() {
		return instance;
	}

	public String getStringVal(String key) {
		return (String) props.get(key);
	}

	public Integer getIntegerVal(String key) {
		Object obj = props.get(key);

		if (obj != null && obj.toString().trim().length() > 0) {
			String val = String.valueOf(obj);
			return isNumber(val) ? Integer.valueOf(val) : 0;
		}

		return 0;
	}

	public Long getLongVal(String key) {
		Object obj = props.get(key);

		if (obj != null && obj.toString().trim().length() > 0) {
			String val = String.valueOf(obj);
			return isNumber(val) ? Long.valueOf(val) : 0;
		}

		return 0L;
	}

	public Date getDateVal(String key) {
		Object date = props.get(key);

		if (date == null || String.valueOf(date).trim().length() == 0) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d = null;

		try {
			d = sdf.parse(String.valueOf(date));
		} catch (ParseException e) {
		}

		return d;
	}

	public static boolean isNumber(String value) {
		Matcher mer = Pattern.compile("^[+-]?[0-9]+$").matcher(value);
		return mer.find();
	}
	
	public String[] getArray(String key) {
		Object obj = props.get(key);
		if (obj == null || String.valueOf(obj).trim().length() == 0) {
			return null;
		}
		String str = String.valueOf(obj);
		String[] array = str.split(",");
		return array;
	}
}
