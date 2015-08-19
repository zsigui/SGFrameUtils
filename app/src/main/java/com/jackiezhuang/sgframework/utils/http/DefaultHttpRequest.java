package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.CommonUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * 默认{@link IHttpRequest}实现类,完成基本的默认设置,可在此基础上重写
 * <p/>
 * Created by zsigui on 15-8-19.
 */
public class DefaultHttpRequest implements IHttpRequest {

	String method = HttpParam.METHOD_GET;

	/**
	 * HTTP请求构造器,默认请求方法{@link HttpParam#METHOD_GET}
	 */
	public DefaultHttpRequest() {
	}

	/**
	 * HTTP请求构造器,可以设置请求方法
	 *
	 * @see HttpParam#METHOD_GET
	 * @see HttpParam#METHOD_POST
	 * @see HttpParam#METHOD_PUT
	 * @see HttpParam#METHOD_DELETE
	 */
	public DefaultHttpRequest(String method) {
		setMethod(method);
	}

	public String getMethod() {
		return method;
	}

	/**
	 * 设置Http请求方法名
	 *
	 * @see HttpParam#METHOD_GET
	 * @see HttpParam#METHOD_POST
	 * @see HttpParam#METHOD_PUT
	 * @see HttpParam#METHOD_DELETE
	 */
	public void setMethod(String method) {
		if (CommonUtil.isEmpty(method))
			return;
		this.method = method.toUpperCase();
	}

	@Override
	public void beforeConnect(HttpURLConnection urlConnection) throws IOException {
		if (HttpParam.METHOD_PUT.equals(method) || HttpParam.METHOD_POST.equals(method)) {
			urlConnection.setDoOutput(true);
		} else {
			urlConnection.setDoOutput(false);
		}
		urlConnection.setDoInput(true);
		urlConnection.setDefaultUseCaches(false);
		// 设置请求头参数
		urlConnection.setRequestProperty(HttpParam.PROP_CONNECTION, "Keep-Alive");
		urlConnection.setRequestProperty(HttpParam.PROP_ACCEPT_ENCODING, "gzip, deflate");
		urlConnection.setRequestProperty(HttpParam.PROP_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
	}

	@Override
	public void afterConnect(HttpURLConnection urlConnection) throws IOException {

	}
}
