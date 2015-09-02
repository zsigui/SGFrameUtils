package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.StringUtil;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by zsigui on 15-8-21.
 */
public class HttpUtil {
	private static final String PRE_TAG = HttpUtil_old.class.getName();

	/**
	 * 从给定contentType和返回HTML正文中解析出编码,如果解析不出则使用默认编码
	 */
	public static String parseCharset(String contentType, byte[] bodyContent, String defaultCharset) {
		String result = defaultCharset;
		try {
			int index = contentType.indexOf("charset=");
			if (index != -1)
				result = contentType.substring(index + 8).toUpperCase();
			else {
				byte[] tmp = new byte[bodyContent.length >= 4096 ? 1024 : bodyContent.length / 4];
				CommonUtil.copy(bodyContent, 0, tmp, 0, tmp.length);
				result = StringUtil.findMatch(CommonUtil.bytesToStr(tmp),
						"<meta[\\s\\S]*?Content-Type[\\s\\S]*?charset=([\\S]*?)\"[\\s]*?>", 1, defaultCharset, false);
			}
		} catch (IndexOutOfBoundsException e) {
		}
		if (CommonUtil.isEmpty(result)) {
			result = defaultCharset;
		}
		return result;
	}

	public static Map<String, String> parseResponseHeader(Map<String, String> headers) {
		return null;
	}

	/**
	 * 构造发送的正文键值格式参数字符串
	 *
	 * @param data 键值参数
	 */
	public static String createKeyValString(final Map<String, String> data) {
		final StringBuilder result = new StringBuilder();
		try {
			if (data != null && data.size() > 0) {
				for (Map.Entry<String, String> param : data.entrySet()) {
					result.append(URLEncoder.encode(param.getKey(), SGConfig.DEFAULT_UTF_CHARSET));
					result.append("=");
					result.append(URLEncoder.encode(param.getValue(), SGConfig.DEFAULT_UTF_CHARSET));
					result.append("&");
				}
				result.deleteCharAt(result.length() - 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	public static boolean isGzipStream(final HttpURLConnection urlConnection) {
		String encoding = urlConnection.getContentEncoding();
		return encoding != null && encoding.contains("gzip");
	}
}