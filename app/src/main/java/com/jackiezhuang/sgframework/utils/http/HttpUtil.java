package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zsigui on 15-8-21.
 */
public class HttpUtil {
	private static final String PRE_TAG = HttpUtil.class.getName();

	/**
	 * 从给定contentType和返回HTML正文中解析出编码,如果解析不出则使用默认编码
	 */
	public static String parseCharset(String contentType, byte[] bodyContent, String defaultCharset) {
		String result = null;
		try {
			int index = contentType.indexOf("charset");
			if (index == -1) {
				byte[] tmp = new byte[bodyContent.length >= 4096 ? 1024 : bodyContent.length / 4];
				CommonUtil.copy(bodyContent, 0, tmp, 0, tmp.length);
				String partContent = CommonUtil.bytesToStr(tmp, SGConfig.DEFAULT_ISO_CHARSET);
				index = partContent.indexOf("charset");
				if (index != -1) {
					result = subString(partContent, index);
				}
			} else {
				result = subString(contentType, index);
			}

		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		if (CommonUtil.isEmpty(result)) {
			result = defaultCharset;
		}
		return result;
	}

	/**
	 * 从指定下标开始切取非` `/`"`/`'`的字符串
	 */
	private static String subString(String contentType, int index) {
		String result = null;
		int start = index + 8;
		char tmp;
		for (; start < contentType.length(); start++) {
			tmp = contentType.charAt(start);
			if (tmp != ' ' && tmp != '"' && tmp != '\'') {
				break;
			}
		}
		if (start < contentType.length()) {
			int end = start + 1;
			for (; end < contentType.length(); end++) {
				tmp = contentType.charAt(end);
				if (tmp == ' ' || tmp == '"' || tmp == '\'') {
					break;
				}
			}
			result = contentType.substring(start, end).toUpperCase();
		}
		return result;
	}

	/**
	 * 解析返回的头信息
	 */
	public static Map<String, String> parseResponseHeader(Map<String, String> headers) {
		Map<String, String> result = new HashMap<>();
		for (Map.Entry<String, String> head : headers.entrySet()) {
			if (!CommonUtil.isEmpty(head)) {
				result.put(head.getKey(), head.getValue());
			}
		}
		return result;
	}

	/**
	 * 判断是否是gzip压缩流
	 */
	public static boolean isGzipStream(final HttpURLConnection urlConnection) {
		String encoding = urlConnection.getContentEncoding();
		return encoding != null && encoding.contains("gzip");
	}
}