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

	private static byte[] work(String requestUrl, IRequest requestOp) {
		byte[] result = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(requestUrl);
			urlConnection = (HttpURLConnection) url.openConnection();

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

			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream in = urlConnection.getInputStream();
				result = IOUtil.readBytes(in);
				in.close();
			} else {
				L.i(PRE_TAG, "work return failed : code = " + urlConnection.getResponseCode() + ", " +
						"msg = " + urlConnection.getResponseMessage());
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
		byte[] result = work(realUrl, new IRequest() {
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
		StringBuilder param = new StringBuilder();
		if (!CommonUtil.isEmpty(requestData)) {
			try {
				for (Map.Entry<String, String> item : requestData.entrySet()) {
					param.append(URLEncoder.encode(item.getKey(), SGConfig.DEFAULT_UTF_CHARSET));
					param.append("=");
					param.append(URLEncoder.encode(item.getValue(), SGConfig.DEFAULT_UTF_CHARSET));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return doGet(requestUrl, param.toString());
	}


	public static void doPost(String requestUrl, Map<String, String> data) throws IOException {
		// 使用Url
		URL url = new URL(requestUrl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod(METHOD_POST);
		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(true);
		urlConnection.setConnectTimeout(sConnTimeout);
		urlConnection.setReadTimeout(sReadTimeout);
		urlConnection.setDefaultUseCaches(false);

		// 此部分设置传参数或上传文件
		urlConnection.setChunkedStreamingMode(0);   // 未知文件大小
		//urlConnection.setFixedLengthStreamingMode(10000); //已知文件大小

		StringBuilder paramStr = new StringBuilder();
		if (data != null && data.size() > 0) {
			for (Map.Entry<String, String> param : data.entrySet()) {
				paramStr.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				paramStr.append("=");
				paramStr.append(URLEncoder.encode(param.getValue(), "UTF-8"));
				paramStr.append("&");
			}
			paramStr.deleteCharAt(paramStr.length() - 1);
		}

		// 设置请求头参数
		urlConnection.setRequestProperty("Connection", "Keep-Alive");
		urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		urlConnection.setRequestProperty("ContentType", "application/x-www-form-urlencoded; charset=UTF-8");
		urlConnection.setRequestProperty("Content-Length", String.valueOf(paramStr.length()));

		urlConnection.connect();

		// post表单
		DataOutputStream bos = new DataOutputStream(urlConnection.getOutputStream());
		bos.write(paramStr.toString().getBytes());
		bos.flush();
		bos.close();


		if (urlConnection.getResponseCode() == 200) {
			byte[] buf = new byte[1024];
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
			String result = CommonUtil.toString(IOUtil.readBytes(in), charset);
		} else {
			L.i(PRE_TAG, "doPost return failed : code = " + urlConnection.getResponseCode() + ", " +
					"msg = " + urlConnection.getResponseMessage());
		}
		urlConnection.disconnect();

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
