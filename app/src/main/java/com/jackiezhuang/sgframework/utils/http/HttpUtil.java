package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.StringUtil;
import com.jackiezhuang.sgframework.utils.T;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by zsigui on 15-8-21.
 */
public class HttpUtil {
	private static final String PRE_TAG = HttpUtil_old.class.getName();

	private static int sConnTimeout = HttpParam.TIMEOUT_CONN_DEFAULT;
	private static int sReadTimeout = HttpParam.TIMEOUT_READ_DEFAULT;
	private static HashMap<String, String> charsetMap = new HashMap<>();
	private static Object mapLock = new Object();
	private static final String BOUNDARY = "-----------------sg1e024" + String.valueOf(System.currentTimeMillis());

	/**
	 * 设置所有Http请求连接超时时间,默认为{@link HttpParam#TIMEOUT_CONN_DEFAULT}
	 */
	public static void setConnTimeout(int timeout) {
		sConnTimeout = timeout;
	}

	/**
	 * 设置所有Http请求读取数据超时时间,默认为{@link HttpParam#TIMEOUT_READ_DEFAULT}
	 */
	public static void setReadTimeout(int timeout) {
		sReadTimeout = timeout;
	}

	/**
	 * 从给定contentType和返回HTML正文中解析出编码,如果解析不出则使用默认编码
	 */
	private static String parseCharset(String contentType, byte[] bodyContent, String defaultCharset) {
		String result = defaultCharset;
		try {
			int index = contentType.indexOf("charset=");
			if (index != -1)
				result = contentType.substring(index + 8).toUpperCase();
			else {
				byte[] tmp = new byte[bodyContent.length >= 4096 ? 1024 : bodyContent.length / 4];
				CommonUtil.copy(bodyContent, 0, tmp, 0, tmp.length);
				result = StringUtil.findMatch(CommonUtil.bytesToStr(tmp),
						"<meta[\\s\\S]*?Content-Type[\\s\\S]*?charset=([\\S]*?)\"[\\s]*?>", 1, defaultCharset);
			}
		} catch (IndexOutOfBoundsException e) {
		}
		if (CommonUtil.isEmpty(result)) {
			result = defaultCharset;
		}
		return result;
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

	/**
	 * 进行HTTP请求需要保证地址前缀http://,该方法用于添加前缀
	 *
	 * @param requestUrl
	 * @return
	 */
	public static String addUrlHead(String requestUrl) {
		String realUrl = requestUrl;
		if (!realUrl.startsWith("http://")) {
			realUrl = "http://" + realUrl;
		}
		return realUrl;
	}

	/**
	 * 构造进行Get操作的Url字符串
	 */
	public static String createUrl(String reqUrl, String params) {
		String realUrl = addUrlHead(reqUrl);
		if (!CommonUtil.isEmpty(params)) {
			int index = realUrl.lastIndexOf('?');
			if (index == realUrl.length() - 1) {
				realUrl += params;
			} else if (index == -1) {
				realUrl += '?' + params;
			} else {
				index = realUrl.lastIndexOf('&');
				if (index == realUrl.length() - 1) {
					realUrl += params;
				} else {
					realUrl += '&' + params;
				}
			}
		}
		return realUrl;
	}


	/**
	 * HttpUrlConnection对象的默认配置流程
	 */
	private static void defaultConfig(IHttpAction requestOp, HttpURLConnection urlConnection) throws IOException {
		// 此部分进行默认设置,可以在IRequest回调函数中进行重配
		urlConnection.setRequestMethod(HttpParam.METHOD_GET);
		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(false);
		urlConnection.setConnectTimeout(sConnTimeout);
		urlConnection.setReadTimeout(sReadTimeout);
		urlConnection.setDefaultUseCaches(false);

		requestOp.beforeConnect(urlConnection);
		urlConnection.connect();
		requestOp.afterConnect(urlConnection);
	}

	public static boolean isGzipStream(final HttpURLConnection urlConnection) {
		String encoding = urlConnection.getContentEncoding();
		return encoding != null && encoding.contains("gzip");
	}


	/**
	 * 执行Http请求并返回字节数组结果
	 * <p>需要在{@link IHttpAction}的beforeRequest方法设置Http请求类型<p/>
	 * <p>默认为GET请求<p/>
	 */
	public static T requestForBytesResponse(String requestUrl, IHttpAction<T> requestOp) {

		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".requestForBytesResponse : param requestUrl(String) is " +
					"null");
		}
		if (CommonUtil.isEmpty(requestOp)) {
			requestOp = new DefaultHttpAction();
		}

		T result = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(requestUrl);
			urlConnection = (HttpURLConnection) url.openConnection();

			// 此部分进行默认设置,可以在IRequest回调函数中进行重配
			defaultConfig(requestOp, urlConnection);

			int statusCode = urlConnection.getResponseCode();
			if (statusCode == HttpURLConnection.HTTP_MOVED_PERM ||
					statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				// 301/302 重定向地址,解析地址重新请求

			} else if (statusCode == HttpURLConnection.HTTP_NOT_MODIFIED){
				// 304 使用缓存

			} else if (statusCode == HttpURLConnection.HTTP_OK) {
				// 200 请求成功
				InputStream in = urlConnection.getInputStream();
				if (isGzipStream(urlConnection)) {
					// 使用Gzip流方式进行读取
					in = new GZIPInputStream(in);
				}
				byte[] data = IOUtil.readBytes(in);

			} else {
				// 其它返回码,错误

				L.i(PRE_TAG, "requestForBytesResponse return failed : code = " + urlConnection.getResponseCode() +
						", msg = " + urlConnection.getResponseMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return requestOp.onResponse(new HttpResponse());
	}
}
