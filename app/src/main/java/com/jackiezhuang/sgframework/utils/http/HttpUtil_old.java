package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.MultiPart;
import com.jackiezhuang.sgframework.utils.http.bean.TextPart;
import com.jackiezhuang.sgframework.utils.io.IOUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by zsigui on 15-8-11.
 */
public final class HttpUtil_old {
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
	 * 构造发送的正文参数字符串
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
	public static String fixUrl(String requestUrl) {
		String realUrl = requestUrl;
		if (!realUrl.startsWith("http://")) {
			realUrl = "http://" + realUrl;
		}
		return realUrl;
	}

	/**
	 * 构造进行Get操作的Url字符串
	 */
	public static String createGetUrl(String reqUrl, String params) {
		String realUrl = fixUrl(reqUrl);
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

	/**
	 * 执行Http请求并返回字节数组结果
	 * <p>需要在{@link IHttpAction}的beforeRequest方法设置Http请求类型<p/>
	 * <p>默认为GET请求<p/>
	 */
	public static byte[] requestForBytesResponse(String requestUrl, IHttpAction requestOp) {

		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".requestForBytesResponse : param requestUrl(String) is " +
					"null");
		}
		if (CommonUtil.isEmpty(requestOp)) {
			requestOp = new DefaultHttpAction();
		}

		byte[] result = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(requestUrl);
			urlConnection = (HttpURLConnection) url.openConnection();

			// 此部分进行默认设置,可以在IRequest回调函数中进行重配
			defaultConfig(requestOp, urlConnection);

			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

				String encoding = urlConnection.getContentEncoding();
				InputStream in = urlConnection.getInputStream();
				if (encoding != null && encoding.contains("gzip")) {
					// 使用Gzip流方式进行读取
					in = new GZIPInputStream(in);
				}
				result = IOUtil.readBytes(in);
			} else {
				L.i(PRE_TAG, "requestForBytesResponse return failed : code = " + urlConnection.getResponseCode() +
						", msg = " + urlConnection.getResponseMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return result;
	}

	/**
	 * 进行Http的Get或Delete操作
	 *
	 * @param requestUrl  请求进行Get或Delete的Url字符串
	 * @param requestData Get或Delete参数,用户需要使用UTF-8进行UrlEncode,null或""表示不传
	 * @param method      GET或DELETE字符串方法名
	 */
	private static byte[] doGetOrDelete(final String requestUrl, String requestData, String method) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doGet : param requestUrl(String) is null");
		}
		String realUrl = createGetUrl(requestUrl, requestData);
		byte[] result = requestForBytesResponse(realUrl, new DefaultHttpAction(method) {

			@Override
			public void afterConnect(HttpURLConnection urlConnection) throws IOException {

				// 获取Charset
				String contentType = urlConnection.getContentType();
				String charset = SGConfig.DEFAULT_UTF_CHARSET;
				try {
					charset = contentType.substring(contentType.indexOf("charset=") + 8).toUpperCase();
				} catch (IndexOutOfBoundsException e) {
				}
				synchronized (mapLock) {
					charsetMap.put(requestUrl, charset);
				}
			}
		});
		return result;
	}

	/**
	 * 进行Http的Get或Delete操作
	 *
	 * @param requestUrl  请求进行Get或Delete的Url字符串
	 * @param requestData Get或Delete参数,用户需要使用UTF-8进行UrlEncode,null或""表示不传
	 * @param method      GET或DELETE字符串方法名
	 */
	private static String doGetOrDeleteString(String requestUrl, String requestData, String method) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doGetString : param requestUrl(String) is null");
		}
		String result = null;
		try {
			byte[] temp = doGetOrDelete(requestUrl, requestData, method);
			String charset = charsetMap.get(requestUrl);
			synchronized (mapLock) {
				charsetMap.remove(requestUrl);
			}
			result = new String(temp, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 进行Http的Post或Put操作
	 *
	 * @param requestUrl  请求执行Post或Put地址
	 * @param requestData Post或Put参数,无则传null
	 * @param method      POST或PUT字符串方法名
	 */
	private static byte[] doPostOrPut(final String requestUrl, Map<String, String> requestData, String method) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doPost : param requestUrl(String) is null");
		}
		final String paramStr = createKeyValString(requestData);
		String realUrl = createGetUrl(requestUrl, null);
		byte[] result = requestForBytesResponse(realUrl, new DefaultHttpAction(method) {
			@Override
			public void beforeConnect(HttpURLConnection urlConnection) throws IOException {
				if (!CommonUtil.isEmpty(paramStr)) {
					// 此部分设置传参数或上传文件
					urlConnection.setDoOutput(true);
					//urlConnection.setChunkedStreamingMode(0);   // 未知文件大小
					urlConnection.setFixedLengthStreamingMode(paramStr.length()); //已知文件大小
				}
			}

			@Override
			public void afterConnect(HttpURLConnection urlConnection) throws IOException {
				// 获取Charset
				String contentType = urlConnection.getContentType();
				String charset = SGConfig.DEFAULT_UTF_CHARSET;
				try {
					charset = contentType.substring(contentType.indexOf("charset=") + 8).toUpperCase();
				} catch (IndexOutOfBoundsException e) {
				}
				synchronized (mapLock) {
					charsetMap.put(requestUrl, charset);
				}

				// post表单
				if (CommonUtil.isEmpty(paramStr)) {
					return;
				}
				IOUtil.writeBytes(urlConnection.getOutputStream(), paramStr, SGConfig.DEFAULT_SYS_CHARSET);
			}
		});
		return result;
	}

	/**
	 * 进行Http的Post或Put操作
	 *
	 * @param requestUrl  请求执行Post或Put地址
	 * @param requestData Post或Put参数,无则传null
	 * @param method      POST或PUT字符串方法名
	 */
	private static String doPostOrPutString(String requestUrl, Map<String, String> requestData, String method) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doPost : param requestUrl(String) is null");
		}
		String result = null;
		try {
			byte[] temp = doPostOrPut(requestUrl, requestData, method);
			String charset = charsetMap.get(requestUrl);
			synchronized (mapLock) {
				charsetMap.remove(requestUrl);
			}
			result = new String(temp, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}


	public static byte[] doPostMultipart(final String requestUrl, final Map<String, Object> requestData,
	                                     String method) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doPost : param requestUrl(String) is null");
		}
		String realUrl = createGetUrl(requestUrl, null);
		byte[] result = requestForBytesResponse(realUrl, new DefaultHttpAction(method) {
			@Override
			public void beforeConnect(HttpURLConnection urlConnection) throws IOException {
				if (!CommonUtil.isEmpty(requestData)) {
					// 此部分设置传参数或上传文件
					urlConnection.setDoOutput(true);
					urlConnection.setChunkedStreamingMode(0);   // 未知文件大小
					urlConnection.setRequestProperty(HttpParam.PROP_CONTENT_TYPE, "multipart/form-data; boundary = " +
							BOUNDARY);
					urlConnection.setRequestProperty(HttpParam.PROP_CACHE_CONTROL, "no-cache");
				}
			}

			@Override
			public void afterConnect(HttpURLConnection urlConnection) throws IOException {
				// 获取Charset
				String contentType = urlConnection.getContentType();
				String charset = SGConfig.DEFAULT_UTF_CHARSET;
				try {
					charset = contentType.substring(contentType.indexOf("charset=") + 8).toUpperCase();
				} catch (IndexOutOfBoundsException e) {
				}
				synchronized (mapLock) {
					charsetMap.put(requestUrl, charset);
				}

				// post表单
				if (CommonUtil.isEmpty(requestData)) {
					return;
				}

				DataOutputStream dout = new DataOutputStream(urlConnection.getOutputStream());
				for (Map.Entry<String, Object> entry : requestData.entrySet()) {
					dout.writeBytes("--");
					dout.writeBytes(BOUNDARY);
					dout.writeBytes("\r\n");
					dout.writeBytes("Contetn-Type:Disposition: form-data; name=");
					dout.writeBytes(entry.getKey());
					if (entry.getValue() instanceof TextPart) {
						TextPart part = (TextPart) entry.getValue();
						dout.writeBytes("\r\nContent-Type: ");
						dout.writeBytes(part.getContentType());
						dout.writeBytes("\r\n\r\n");
						dout.writeBytes(part.getVal());
					} else if (entry.getValue() instanceof MultiPart) {
						MultiPart part = (MultiPart) entry.getValue();
						dout.writeBytes("; ");
						dout.writeBytes("filename=");
						dout.writeBytes(part.getFileName());
						dout.writeBytes("\r\nContent-Type: ");
						dout.writeBytes(part.getContentType());
						dout.writeBytes("\r\n");
						if (!CommonUtil.isEmpty(part.getContentEndcoding())) {
							dout.writeBytes("Content-Transfer-Encoding: ");
							dout.writeBytes(part.getContentEndcoding());
						}
						dout.writeBytes("\r\n");
						dout.writeBytes(part.getVal());
					}

				}
				dout.writeBytes("\r\n--");
				dout.writeBytes(BOUNDARY);
				dout.writeBytes("--\r\n");
				dout.flush();
				dout.close();

			}
		});
		return result;
	}

	/**
	 * 进行Http的Get操作
	 *
	 * @param requestUrl 请求进行Get的Url字符串
	 */
	public static byte[] doGet(String requestUrl) {
		return doGet(requestUrl, "");
	}

	/**
	 * 进行Http的Get操作
	 *
	 * @param requestUrl  请求进行Get的Url字符串
	 * @param requestData get参数,用户需要使用UTF-8进行UrlEncode,""表示不传
	 */
	public static byte[] doGet(String requestUrl, String requestData) {
		return doGetOrDelete(requestUrl, requestData, HttpParam.METHOD_GET);
	}

	/**
	 * 进行Http的Delete操作
	 *
	 * @param requestUrl 请求进行Delete的Url字符串
	 */
	public static byte[] doDelete(String requestUrl) {
		return doDelete(requestUrl, "");
	}

	/**
	 * 进行Http的Delete操作
	 *
	 * @param requestUrl  请求进行Delete的Url字符串
	 * @param requestData Delete参数,用户需要使用UTF-8进行UrlEncode,""表示不传
	 */
	public static byte[] doDelete(String requestUrl, String requestData) {
		return doGetOrDelete(requestUrl, requestData, HttpParam.METHOD_DELETE);
	}

	/**
	 * 进行Http的Get操作
	 *
	 * @param requestUrl  请求进行Get的Url字符串
	 * @param requestData get参数,会默认使用UTF-8进行UrlEncode,null表示不传
	 */
	public static byte[] doGet(String requestUrl, Map<String, String> requestData) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doGet : param requestUrl(String) is null");
		}
		return doGet(requestUrl, createKeyValString(requestData));
	}

	/**
	 * 进行Http的Delete操作
	 *
	 * @param requestUrl  请求进行Delete的Url字符串
	 * @param requestData Delete参数,用户需要使用UTF-8进行UrlEncode,""表示不传
	 */
	public static byte[] doDelete(String requestUrl, Map<String, String> requestData) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doGet : param requestUrl(String) is null");
		}
		return doDelete(requestUrl, createKeyValString(requestData));
	}

	/**
	 * 进行Http的Get操作
	 *
	 * @param requestUrl  请求进行Get的Url字符串
	 * @param requestData get参数,用户需要使用UTF-8进行UrlEncode,null表示不传
	 */
	public static String doGetString(String requestUrl, String requestData) {
		return doGetOrDeleteString(requestUrl, requestData, HttpParam.METHOD_GET);
	}

	/**
	 * 进行Http的Delete操作
	 *
	 * @param requestUrl  请求进行Delete的Url字符串
	 * @param requestData delete参数,用户需要使用UTF-8进行UrlEncode,""表示不传
	 */
	public static String doDeleteString(String requestUrl, String requestData) {
		return doGetOrDeleteString(requestUrl, requestData, HttpParam.METHOD_POST);
	}

	/**
	 * 进行Http的Get操作
	 *
	 * @param requestUrl  请求进行Get的Url字符串
	 * @param requestData get参数,会默认使用UTF-8进行UrlEncode,null表示不传
	 */
	public static String doGetString(String requestUrl, Map<String, String> requestData) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doGeString : param requestUrl(String) is null");
		}
		return doGetString(requestUrl, createKeyValString(requestData));
	}

	/**
	 * 进行Http的Delete操作
	 *
	 * @param requestUrl  请求进行Delete的Url字符串
	 * @param requestData Delete参数,会默认使用UTF-8进行UrlEncode,null表示不传
	 */
	public static String doDeleteString(String requestUrl, Map<String, String> requestData) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doGeString : param requestUrl(String) is null");
		}
		return doDeleteString(requestUrl, createKeyValString(requestData));
	}

	/**
	 * 进行Http的Post操作
	 *
	 * @param requestUrl  请求执行Post地址
	 * @param requestData Post参数,无则传null
	 */
	public static byte[] doPost(String requestUrl, Map<String, String> requestData) {
		return doPostOrPut(requestUrl, requestData, HttpParam.METHOD_POST);
	}

	/**
	 * 进行Http的Put操作
	 *
	 * @param requestUrl  请求执行Put地址
	 * @param requestData Put参数,无则传null
	 */
	public static byte[] doPut(String requestUrl, Map<String, String> requestData) {
		return doPostOrPut(requestUrl, requestData, HttpParam.METHOD_PUT);
	}

	/**
	 * 进行Http的Post操作
	 *
	 * @param requestUrl  请求执行Post地址
	 * @param requestData Post参数,无则传null
	 */
	public static String doPostString(String requestUrl, Map<String, String> requestData) {
		return doPostOrPutString(requestUrl, requestData, HttpParam.METHOD_POST);
	}

	/**
	 * 进行Http的Put操作
	 *
	 * @param requestUrl  请求执行Put地址
	 * @param requestData Put参数,无则传null
	 */
	public static String doPutString(String requestUrl, Map<String, String> requestData) {
		return doPostOrPutString(requestUrl, requestData, HttpParam.METHOD_PUT);
	}


}