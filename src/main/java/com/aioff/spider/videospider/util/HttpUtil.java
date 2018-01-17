package com.aioff.spider.videospider.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpUtil {
	
	public static String getHTML(String url) {
		List<String> cmds = new ArrayList<String>();
		cmds.add("D:/Program Files/phantomjs-2.1.1-windows/bin/phantomjs.exe");
		cmds.add("E:/spider/hello.js");
		try {
			ProcessBuilder builder = new ProcessBuilder(cmds);
			Process process = builder.start();
			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuffer sbf = new StringBuffer();
			String tmp = "";
			while ((tmp = br.readLine()) != null) {
				sbf.append(tmp);
			}
			return sbf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
