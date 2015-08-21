package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * 请求接口
 * @author zsigui
 */
public interface IHttpAction<T> {
	/**
	 * 进行connect()连接之前执行的操作,用于配置请求连接的参数,包括请求方法
	 *
	 * @param urlConnection Http连接对象
	 * @throws java.io.IOException
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

	public T onResponse(HttpResponse response);
}
