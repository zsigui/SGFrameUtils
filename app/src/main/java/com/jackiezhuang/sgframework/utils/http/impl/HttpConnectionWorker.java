package com.jackiezhuang.sgframework.utils.http.impl;

import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.StringUtil;
import com.jackiezhuang.sgframework.utils.common.ByteArrayPool;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.HttpConfig;
import com.jackiezhuang.sgframework.utils.http.SGDefaultHostnameVerifier;
import com.jackiezhuang.sgframework.utils.http.SGDefaultX509TrustManager;
import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IHttpWorker;
import com.jackiezhuang.sgframework.utils.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	/**
	 * 从给定contentType和返回HTML正文中解析出编码,如果解析不出则使用默认编码
	 */
	private String parseCharset(String contentType, byte[] bodyContent, String defaultCharset) {
		String result = defaultCharset;
		byte[] tmp = null;
		try {
			int index = contentType.indexOf("charset=");
			if (index != -1)
				result = contentType.substring(index + 8).toUpperCase();
			else {
				tmp = ByteArrayPool.init().obtain(bodyContent.length >= 4096 ? 1024 : bodyContent.length / 4);
				CommonUtil.copy(bodyContent, 0, tmp, 0, tmp.length);
				result = StringUtil.findMatch(CommonUtil.bytesToStr(tmp),
						"<meta[\\s\\S]*?Content-Type[\\s\\S]*?charset=([\\S]*?)\"[\\s]*?>", 1, defaultCharset, false);
			}
		} catch (IndexOutOfBoundsException e) {
			// Nothing to do here
		} finally {
			if (!CommonUtil.isEmpty(tmp)) {
				ByteArrayPool.init().add(tmp);
			}
		}
		if (CommonUtil.isEmpty(result)) {
			result = defaultCharset;
		}
		return result;
	}

	@Override
	public HttpResponse performRequest(HttpRequest request, Map<String, String> additionalHeaders) throws IOException,
			SGHttpException {
		HttpURLConnection connection = openConnection(new URL(request.getUrl()));
		writeOutputData(request, connection);
		connection.setRequestMethod(request.getMethod().val());
		addHeaders(request, additionalHeaders, connection);


		InputStream in;
		try {
			in = connection.getInputStream();
		} catch (Exception e) {
			in = connection.getErrorStream();
		}

		// 设置返回响应信息
		HttpResponse result = new HttpResponse();
		result.setBodyContent(IOUtil.readBytes(in));
		result.setStatusCode(connection.getResponseCode());
		result.setParsedEncoding(parseCharset(request.getContentType(), result.getBodyContent(),
				SGConfig.DEFAULT_UTF_CHARSET));
		result.setModified(!(connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED));
		result.setSuccess(connection.getResponseCode() == HttpURLConnection.HTTP_OK && connection.getResponseCode() ==
				HttpURLConnection.HTTP_NOT_MODIFIED);
		result.setHeaders(parseHeaders(connection));

		connection.disconnect();

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
				for (String v : header.getValue()) {
					value += (v + "; ");
				}
				headers.put(header.getKey(), value);
			}
		}
		return headers;
	}

	/**
	 * 往请求连接中添加所有头属性信息
	 */
	private void addHeaders(HttpRequest request, Map<String, String> additionalHeaders, HttpURLConnection connection) {
		Map<String, String> headers = new HashMap<>();
		headers.putAll(additionalHeaders);
		headers.putAll(request.getRequestHeaders());
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
