package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.DateUtil;
import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.CacheEntry;
import com.jackiezhuang.sgframework.utils.http.bean.NetworkResponse;
import com.jackiezhuang.sgframework.utils.io.IOUtil;

import java.net.HttpURLConnection;

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
	public static CacheEntry parseResponseHeader(NetworkResponse response) {
		CacheEntry entry = new CacheEntry();

		// 服务器是否有声明缓存控制策略
		boolean hasCacheControl = false;
		// 服务器返回时的响应时间
		long serverDate = 0;
		// 缓存的有效时间
		long maxAge = 0;
		String serverEtag = null;
		long now = System.currentTimeMillis();
		String tempStr;

		tempStr = response.getHeaders().get("Date");
		serverDate = (tempStr != null ? DateUtil.parseGMTDate(tempStr).getTime() : now);

		tempStr = response.getHeaders().get("Cache-Control");
		if (tempStr != null) {
			hasCacheControl = true;
			String[] tokens = tempStr.split(",");
			for (int i = 0; i < tokens.length; i++) {
				String token = tokens[i].trim();
				if (token.equals("no-cache") || token.equals("no-store")) {
					return null;
				} else if (token.startsWith("max-age=")) {
					try {
						maxAge = Long.parseLong(token.substring(8));
					} catch (Exception e) {
						// do nothing
					}
				} else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
					// 如果服务器声明必须重新验证，或必须使用代理验证，则相当于本次数据是一次性的
					maxAge = 0;
				}
			}
		}

		long serverExpires = 0;
		tempStr = response.getHeaders().get("Expires");
		if (tempStr != null) {
			serverExpires = DateUtil.parseGMTDate(tempStr).getTime();
		}

		long softExpire = 0;
		serverEtag = response.getHeaders().get("ETag");
		if (hasCacheControl) {
			softExpire = now + maxAge * 1000;
		} else if (serverDate > 0 && serverExpires >= serverDate) {
			softExpire = now + (serverExpires - serverDate);
		}

		entry.setData(IOUtil.readBytes(response.getContent()));
		entry.setEtag(serverEtag);
		entry.setServerTime(serverDate);
		entry.setResponseHeaders(response.getHeaders());
		if (HttpConfig.sUseSeverControl) {
			entry.setExpireTime(softExpire);
		} else {
			entry.setExpireTime(now + HttpConfig.sCacheTime * 60 * 1000);
		}
		return entry;
	}

	/**
	 * 判断是否是gzip压缩流
	 */
	public static boolean isGzipStream(final HttpURLConnection urlConnection) {
		String encoding = urlConnection.getContentEncoding();
		return encoding != null && encoding.contains("gzip");
	}
}