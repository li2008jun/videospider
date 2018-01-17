package com.aioff.spider.videospider.download;

import com.aioff.spider.entity.Request;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Response {

	private Request request;

	private String rawText;

	private int statusCode;

	private boolean needCycleRetry;

	/**
	 * 转发地址
	 */
	private String location;

	/**
	 * 返回数据流
	 */
	private InputStream content;

	private Map<String, String> cookies = new LinkedHashMap<String, String>();

	public Response() {
	}

	public Request getRequest() {
		return request;
	}

	public boolean isNeedCycleRetry() {
		return needCycleRetry;
	}

	public void setNeedCycleRetry(boolean needCycleRetry) {
		this.needCycleRetry = needCycleRetry;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getRawText() {
		return rawText;
	}

	public void setRawText(String rawText) {
		this.rawText = rawText;
	}

	public InputStream getContent() {
		return content;
	}

	public void setContent(InputStream content) {
		this.content = content;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	public void addCookie(String key, String value) {
		if (this.cookies == null) {
			this.cookies = new HashMap<String, String>();
		}

		this.cookies.put(key, value);
	}

	public String getCookieValue() {
		StringBuilder cookieValue = new StringBuilder();
		int len = cookies.size();
		int index = 1;

		for (Entry<String, String> elem : cookies.entrySet()) {
			cookieValue.append(elem.getKey());
			cookieValue.append("=");
			cookieValue.append(elem.getValue());

			if (index < len) {
				cookieValue.append("; ");
			}

			index++;
		}

		return cookieValue.toString().trim();
	}

}
