package com.jackiezhuang.sgframework.utils.http;

/**
 * Created by zsigui on 15-8-19.
 */
public final class HttpParam {

	// HTTP请求的方法
	public static final String METHOD_POST = "POST";
	public static final String METHOD_GET = "GET";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_DELETE = "DELETE";
	public static final String METHOD_OPTIONS = "OPTIONS";
	public static final String METHOD_HEAD = "HEAD";
	public static final String METHOD_TRACE = "TRACE";

	// 请求和读写超时默认时间
	public static final int TIMEOUT_CONN_DEFAULT = 5 * 1000;
	public static final int TIMEOUT_READ_DEFAULT = 30 * 1000;

	// 请求头属性名
	public static final String PROP_HOST = "Host";
	public static final String PROP_CONNECTION = "Connection";
	public static final String PROP_ACCEPT = "Accept";
	public static final String PROP_ACCEPT_ENCODING = "Accept-Encoding";
	public static final String PROP_ACCEPT_LANGUAGE = "Accept-Language";
	public static final String PROP_CONTENT_TYPE = "ContentType";
	public static final String PROP_CONTENT_LENGTH = "Content-Length";
	public static final String PROP_COOKIE = "Cookie";
	public static final String PROP_CACHE_CONTROL = "Cache-Control";
	public static final String PROP_USER_AGENT = "User-Agent";
	public static final String PROP_REFERER = "Referer";
	public static final String PROP_IF_MODIFIED = "If-Modified-Since";

}
