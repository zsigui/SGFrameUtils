package com.jackiezhuang.sgframework.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by zsigui on 15-8-11.
 */
public class HttpUtil {
	private static final String PRE_TAG = HttpUtil.class.getName();
	private static final int MIN_SDK_VERSION = 10;
	public static final String METHOD_POST = "POST";
	public static final String METHOD_GET = "GET";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_DELETE = "DELETE";
	public static final int DEFAULT_CONN_TIMEOUT = 5 * 1000;
	public static final int DEFAULT_READ_TIMEOUT = 30 * 1000;

	private static int sConnTimeout = DEFAULT_CONN_TIMEOUT;
	private static int sReadTimeout = DEFAULT_READ_TIMEOUT;

	/**
	 * 设置所有Http请求连接超时时间,默认为{@link #DEFAULT_CONN_TIMEOUT}
	 */
	public static void setConnTimeout(int timeout) {
		sConnTimeout = timeout;
	}

	/**
	 * 设置所有Http请求读取数据超时时间,默认为{@link #DEFAULT_READ_TIMEOUT}
	 */
	public static void setReadTimeout(int timeout) {
		sReadTimeout = timeout;
	}

	/**
	 * 构造发送的正文参数字符串
	 *
	 * @param data 键值参数
	 */
	private static String constructParams(final Map<String, String> data) {
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
	private static String createGetUrl(String reqUrl, String params) {
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
	private static void defaultConfig(IRequest requestOp, HttpURLConnection urlConnection) throws IOException {
		// 此部分进行默认设置,可以在IRequest回调函数中进行重配
		urlConnection.setRequestMethod(METHOD_GET);
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
	 * <p>需要在{@link com.jackiezhuang.sgframework.utils.HttpUtil.IRequest}的beforeRequest方法设置Http请求类型<p/>
	 * <p>默认为GET请求<p/>
	 */
	public static byte[] requestForBytesResponse(String requestUrl, IRequest requestOp) {
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
				in.close();
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
	 * 执行Http请求并返回解析获取的编码格式字符串
	 * <p>需要在{@link com.jackiezhuang.sgframework.utils.HttpUtil.IRequest}的beforeRequest方法设置Http请求类型<p/>
	 * <p>默认为GET请求<p/>
	 */
	public static String requestForStringResponse(String requestUrl, IRequest requestOp) {
		String result = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(requestUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			defaultConfig(requestOp, urlConnection);

			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

				String encoding = urlConnection.getContentEncoding();
				String contentType = urlConnection.getContentType();
				String charset = SGConfig.DEFAULT_UTF_CHARSET;
				try {
					charset = contentType.substring(contentType.indexOf("charset=") + 8);
				} catch (IndexOutOfBoundsException e) {
				}

				InputStream in = urlConnection.getInputStream();
				if (encoding != null && encoding.contains("gzip")) {
					// 使用Gzip流方式进行读取
					in = new GZIPInputStream(in);
				}
				result = CommonUtil.toString(IOUtil.readBytes(in), charset);
				in.close();
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
	 * 进行Http的Get操作
	 *
	 * @param requestUrl  请求进行Get的Url字符串
	 * @param requestData get参数,用户需要使用UTF-8进行UrlEncode,null表示不传
	 */
	public static byte[] doGet(String requestUrl, String requestData) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doGet : param requestUrl(String) is null");
		}
		String realUrl = createGetUrl(requestUrl, requestData);
		byte[] result = requestForBytesResponse(realUrl, new IRequest() {
			@Override
			public void beforeConnect(HttpURLConnection urlConnection) throws IOException {
				urlConnection.setRequestMethod(METHOD_GET);
				urlConnection.setDoInput(true);
				urlConnection.setDoOutput(false);
				urlConnection.setConnectTimeout(sConnTimeout);
				urlConnection.setReadTimeout(sReadTimeout);
				urlConnection.setDefaultUseCaches(false);
				// 设置请求头参数
				urlConnection.setRequestProperty("Connection", "Keep-Alive");
				urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
				urlConnection.setRequestProperty("ContentType", "application/x-www-form-urlencoded; charset=UTF-8");
			}

			@Override
			public void afterConnect(HttpURLConnection urlConnection) throws IOException {
				// GET无须
			}
		});
		return result;
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
		return doGet(requestUrl, constructParams(requestData));
	}

	/**
	 * 进行Http的Get操作
	 *
	 * @param requestUrl  请求进行Get的Url字符串
	 * @param requestData get参数,用户需要使用UTF-8进行UrlEncode,null表示不传
	 */
	public static String doGetString(String requestUrl, String requestData) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doGetString : param requestUrl(String) is null");
		}
		String realUrl = createGetUrl(requestUrl, requestData);
		String result = requestForStringResponse(realUrl, new IRequest() {
			@Override
			public void beforeConnect(HttpURLConnection urlConnection) throws IOException {
				urlConnection.setRequestMethod(METHOD_GET);
				urlConnection.setDoInput(true);
				urlConnection.setDoOutput(false);
				urlConnection.setConnectTimeout(sConnTimeout);
				urlConnection.setReadTimeout(sReadTimeout);
				urlConnection.setDefaultUseCaches(false);
				// 设置请求头参数
				urlConnection.setRequestProperty("Connection", "Keep-Alive");
				urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
				urlConnection.setRequestProperty("ContentType", "application/x-www-form-urlencoded; charset=UTF-8");
			}

			@Override
			public void afterConnect(HttpURLConnection urlConnection) throws IOException {
				// GET无须
			}
		});
		return result;
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
		return doGetString(requestUrl, constructParams(requestData));
	}

	/**
	 * 进行Http的Post操作
	 *
	 * @param requestUrl 请求执行Post地址
	 * @param data       Post参数,无则传null
	 */
	public static byte[] doPost(String requestUrl, Map<String, String> data) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doPost : param requestUrl(String) is null");
		}
		final String paramStr = constructParams(data);
		String realUrl = createGetUrl(requestUrl, null);
		byte[] result = requestForBytesResponse(realUrl, new IRequest() {
			@Override
			public void beforeConnect(HttpURLConnection urlConnection) throws IOException {
				urlConnection.setRequestMethod(METHOD_POST);
				urlConnection.setDoOutput(true);
				urlConnection.setConnectTimeout(sConnTimeout);
				urlConnection.setReadTimeout(sReadTimeout);
				urlConnection.setDefaultUseCaches(false);
				if (!CommonUtil.isEmpty(paramStr)) {
					// 此部分设置传参数或上传文件
					urlConnection.setDoInput(true);
					urlConnection.setChunkedStreamingMode(0);   // 未知文件大小
					//urlConnection.setFixedLengthStreamingMode(10000); //已知文件大小
				}
				// 设置请求头参数
				urlConnection.setRequestProperty("Connection", "Keep-Alive");
				urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
				urlConnection.setRequestProperty("ContentType", "application/x-www-form-urlencoded; charset=UTF-8");
			}

			@Override
			public void afterConnect(HttpURLConnection urlConnection) throws IOException {
				if (CommonUtil.isEmpty(paramStr)) {
					return;
				}
				// post表单
				DataOutputStream bos = new DataOutputStream(urlConnection.getOutputStream());
				bos.write(paramStr.getBytes(SGConfig.DEFAULT_SYS_CHARSET));
				bos.flush();
				bos.close();
			}
		});
		return result;
	}

	/**
	 * 进行Http的Post操作
	 *
	 * @param requestUrl 请求执行Post地址
	 * @param data       Post参数,无则传null
	 */
	public static String doPostString(String requestUrl, Map<String, String> data) {
		if (CommonUtil.isEmpty(requestUrl)) {
			throw new IllegalArgumentException(PRE_TAG + ".doPost : param requestUrl(String) is null");
		}
		final String paramStr = constructParams(data);
		String realUrl = createGetUrl(requestUrl, null);
		String result = requestForStringResponse(realUrl, new IRequest() {
			@Override
			public void beforeConnect(HttpURLConnection urlConnection) throws IOException {
				urlConnection.setRequestMethod(METHOD_POST);
				urlConnection.setDoOutput(true);
				urlConnection.setConnectTimeout(sConnTimeout);
				urlConnection.setReadTimeout(sReadTimeout);
				urlConnection.setDefaultUseCaches(false);
				if (!CommonUtil.isEmpty(paramStr)) {
					// 此部分设置传参数或上传文件
					urlConnection.setDoInput(true);
					urlConnection.setChunkedStreamingMode(0);   // 未知文件大小
					//urlConnection.setFixedLengthStreamingMode(10000); //已知文件大小
				}
				// 设置请求头参数
				urlConnection.setRequestProperty("Connection", "Keep-Alive");
				urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
				urlConnection.setRequestProperty("ContentType", "application/x-www-form-urlencoded; charset=UTF-8");
			}

			@Override
			public void afterConnect(HttpURLConnection urlConnection) throws IOException {
				if (CommonUtil.isEmpty(paramStr)) {
					return;
				}
				// post表单
				DataOutputStream bos = new DataOutputStream(urlConnection.getOutputStream());
				bos.write(paramStr.getBytes(SGConfig.DEFAULT_SYS_CHARSET));
				bos.flush();
				bos.close();
			}
		});
		return result;
	}

	/**
	 * 请求接口
	 */
	public interface IRequest {
		/**
		 * 进行connect()连接之前执行的操作,用于配置请求连接的参数,包括请求方法
		 *
		 * @param urlConnection Http连接对象
		 * @throws IOException
		 */
		public void beforeConnect(HttpURLConnection urlConnection) throws IOException;

		/**
		 * 进行connect()连接之后立即执行的操作,用于执行传输数据的操作
		 * <p>注意:传输数据前需要在{@link #beforeConnect}中调用urlConnection.setDoOutput(false)<p/>
		 *
		 * @param urlConnection Http连接对象
		 * @throws IOException
		 */
		public void afterConnect(HttpURLConnection urlConnection) throws IOException;
	}
}
