package com.jackiezhuang.sgframework.utils.http.impl;

import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.HttpConfig;
import com.jackiezhuang.sgframework.utils.http.HttpUtil;
import com.jackiezhuang.sgframework.utils.http.SGDefaultHostnameVerifier;
import com.jackiezhuang.sgframework.utils.http.SGDefaultX509TrustManager;
import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.NetworkResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IHttpWorker;
import com.jackiezhuang.sgframework.utils.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Created by zsigui on 15-8-28.
 */
public class HttpConnectionWorker implements IHttpWorker {

	/**
	 * 进行Https连接方式的协议工厂
	 */
	private SSLSocketFactory mSSLSocketFactory;

	public HttpConnectionWorker() {
		this(null);
	}

	public HttpConnectionWorker(SSLSocketFactory sslSocketFactory) {
		try {
			// 设置默认自定义Https的认证方式
			TrustManager[] tm = {new SGDefaultX509TrustManager()};
			SSLContext sslContext = SSLContext.getInstance("TSL");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			HttpsURLConnection.setDefaultSSLSocketFactory(mSSLSocketFactory);
			HttpsURLConnection.setDefaultHostnameVerifier(new SGDefaultHostnameVerifier());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.mSSLSocketFactory = sslSocketFactory;
	}

	public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		if (CommonUtil.isEmpty(sslSocketFactory)) {
			return;
		}
		mSSLSocketFactory = sslSocketFactory;
	}

	@Override
	public NetworkResponse performRequest(HttpRequest request) throws
			IOException,
			SGHttpException {
		HttpURLConnection connection = openConnection(new URL(request.getUrl()));
		writeOutputData(request, connection);
		connection.setRequestMethod(request.getMethod().val());
		addHeaders(request, connection);


		InputStream in;
		try {
			in = connection.getInputStream();
			if (HttpUtil.isGzipStream(connection)) {
				// 使用Gzip流方式进行读取
				in = new GZIPInputStream(in);
			}
		} catch (Exception e) {
			in = connection.getErrorStream();
		}

		// 设置返回响应信息
		NetworkResponse result = new NetworkResponse();
		result.setHeaders(parseHeaders(connection));
		result.setContent(in);
		result.setContentEncoding(connection.getContentEncoding());
		result.setContentLength(connection.getContentLength());
		result.setContentType(connection.getContentType());
		result.setDate(connection.getDate());
		result.setIfModifiedSince(connection.getIfModifiedSince());
		result.setLastModified(connection.getLastModified());
		result.setResponseCode(connection.getResponseCode());

		return result;
	}

	/**
	 * 解析获取返回头属性信息
	 */
	private Map<String, String> parseHeaders(HttpURLConnection connection) {
		Map<String, String> headers = new HashMap<>();
		for (Map.Entry<String, List<String>> header : connection.getHeaderFields()
				.entrySet()) {
			if (header.getKey() != null) {
				String value = "";
				if (header.getKey().equals("Set-Cookie")) {
					for (String v : header.getValue()) {
						int i = v.indexOf(";");
						value += (v.substring(0, i == -1 ? v.length() : i).trim() + "; ");
					}
					headers.put("Cookie", value);
				} else {
					for (String v : header.getValue()) {
						value += (v + "; ");
					}
					headers.put(header.getKey(), value);
				}
			}
		}
		return headers;
	}

	/**
	 * 往请求连接中添加所有头属性信息
	 */
	private void addHeaders(HttpRequest request, HttpURLConnection connection) {
		for (Map.Entry<String, String> header : request.getRequestHeaders().entrySet()) {
			connection.addRequestProperty(header.getKey(), header.getValue());
		}
	}

	/**
	 * 判断并传递正文内容
	 */
	private void writeOutputData(HttpRequest request, HttpURLConnection connection) throws IOException {
		switch (request.getMethod()) {
			case PUT:
			case POST:
				connection.setDoInput(true);
				if (!CommonUtil.isEmpty(request.getOutputData())) {
					connection.setDoOutput(true);
					connection.setFixedLengthStreamingMode(request.getOutputData().length);
					connection.setRequestProperty("Content-Type", request.getContentType());
					IOUtil.writeBytes(connection.getOutputStream(), request.getOutputData());
				}
				break;
		}
	}

	/**
	 * 根据Url打开Http连接
	 */
	private HttpURLConnection openConnection(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setConnectTimeout(HttpConfig.sConnTimeout);
		connection.setReadTimeout(HttpConfig.sReadTimeout);
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(HttpConfig.sFollowRedirect);

		// use caller-provided custom SslSocketFactory, if any, for HTTPS
		if ("https".equals(url.getProtocol()) && mSSLSocketFactory != null) {
			((HttpsURLConnection) connection).setSSLSocketFactory(mSSLSocketFactory);
		}

		return connection;
	}

}
