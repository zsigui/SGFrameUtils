package com.jackiezhuang.sgframework.utils.http.bean;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.chiper.MD5;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.itfc.ResponseCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * HttpRequest基础类，包含基础属性和类型配置
 * <p/>
 * <p></p>
 * Created by zsigui on 15-8-24.
 */
public abstract class HttpRequest implements Comparable<HttpRequest> {

	private static final String TAG = HttpRequest.class.getName();

	/**
	 * 支持的Http请求方法的枚举类
	 */
	public enum Method {

		OPTION("OPTIONS"), HEAD("HEAD"), POST("POST"), GET("GET"), PUT("PUT"), DELETE("DELETE"), TRACE("TRACE");

		private String name;

		private Method(String name) {
			this.name = name;
		}

		public String val() {
			return this.name;
		}
	}

	/**
	 * 进行HTTP请求的优先级
	 */
	public enum Priority {
		LOW, NORMAL, HIGH, IMMEDIATE
	}


	private String mUrl = null;
	private Method mMethod = Method.GET;
	private Map<String, String> mRequestHeaders;
	private boolean mShouldCache;
	private boolean mIsCanceled = false;
	private CacheEntry mCache;
	private ResponseCallback mCallback;
	private boolean mIsDelivery = false;

	/**
	 * 请求优先级序列值
	 */
	private int mSequence;

	/**
	 * 修复请求连接地址
	 * <p></p>
	 * 正常URL = [协议]://[域名]/[路径]
	 */
	private String fixUrl(String url) throws SGHttpException {
		url = url.trim();
		if (url.length() < 7) {
			// 默认添加http://协议头
			url = "http://" + url;
		}

		int index = url.lastIndexOf("://");
		String tmp = url.substring(0, 5).toLowerCase();
		if (index != 4 || index != 5 || (!tmp.startsWith("http://") && !tmp.startsWith("https://"))) {
			throw new SGHttpException(String.format("The Url : '%s' had a bad protocol format and can't be fixed ",
					url));
		}
		index = url.indexOf("/", 8);
		if (index == url.length() - 1) {
			// 类似如 http(s)://xxxx/
			url = url.substring(0, index).toLowerCase();
		} else if (index == -1) {
			// 类似如 http(s)://xxxx/xxx
			tmp = url.substring(0, index).toLowerCase();
			url = tmp + url.substring(index, url.length());
		} else {
			// 类似如 http(s)://xxxx
			url = url.toLowerCase();
		}

		return url;
	}

	/**
	 * 获取请求地址
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * 设置请求地址,同时会对地址进行简单的头协议检查,默认设置为http(若为https,需要自行配置)
	 */
	public void setUrl(String url) throws SGHttpException {
		if (CommonUtil.isEmpty(url)) {
			L.e(TAG, "setUrl : url is not allowed to be null or \"\"");
			//throw new IllegalArgumentException(TAG + ".setUrl : mUrl is not allowed to be null or \"\"");
			return;
		}
		this.mUrl = fixUrl(url);
	}

	/**
	 * 获取请求方法名
	 */
	public Method getMethod() {
		return mMethod;
	}

	/**
	 * 设置Http请求方式
	 */
	public void setMethod(Method method) {
		if (CommonUtil.isEmpty(method)) {
			//throw new IllegalArgumentException(TAG + ".setMethod : method is not allowed to be null");
			return;
		}
		this.mMethod = method;
	}

	/**
	 * 获取Http请求头
	 */
	public Map<String, String> getRequestHeaders() {
		return mRequestHeaders;
	}

	/**
	 * 设置Http请求头
	 */
	public void setRequestHeaders(Map<String, String> requestHeaders) {
		if (CommonUtil.isEmpty(requestHeaders)) {
			return;
		}
		mRequestHeaders = requestHeaders;
	}

	/**
	 * 添加新的http请求头,如果该键值已存在,则会进行组合。有别于 {@link #setRequestHeader}
	 */
	public void addRequestHeader(String key, String val) {
		if (CommonUtil.isEmpty(key) || CommonUtil.isEmpty(val)) {
			L.e(TAG, "addRequestHeader(String, String) : key or val is not allowed to be null");
			return;
		}
		if (CommonUtil.isEmpty(mRequestHeaders)) {
			mRequestHeaders = new HashMap<>();
		}
		if (mRequestHeaders.containsKey(key)) {
			mRequestHeaders.put(key, mRequestHeaders.get(key) + ";" + val);
		}
	}

	/**
	 * 添加或者重新设置http请求头。如果该键值已存在,则为重设,否则添加。有别于 {@link #addRequestHeader}
	 */
	public void setRequestHeader(String key, String val) {
		if (CommonUtil.isEmpty(key) || CommonUtil.isEmpty(val)) {
			L.e(TAG, "setRequestHeader(String, String) : key or val is not allowed to be null");
			return;
		}
		if (CommonUtil.isEmpty(mRequestHeaders)) {
			mRequestHeaders = new HashMap<>();
		}
		mRequestHeaders.put(key, val);
	}

	/**
	 * 判断是否进行缓存控制
	 */
	public boolean shouldCache() {
		return mShouldCache;
	}

	/**
	 * 设置是否进行缓存控制策略,默认为false
	 */
	public void setShouldCache(boolean shouldCache) {
		mShouldCache = shouldCache;
	}

	/**
	 * 指示取消执行该请求
	 */
	public void cancel() {
		mIsCanceled = true;
	}

	/**
	 * 判断请求是否取消
	 */
	public boolean isCanceled() {
		return mIsCanceled;
	}

	/**
	 * 获取请求的优先级别序列值
	 */
	public int getSequence() {
		return mSequence;
	}

	/**
	 * 设置请求优先级别序列值,该值用于同一请求级别时判断比较
	 */
	public void setSequence(int sequence) {
		mSequence = sequence;
	}

	/**
	 * 获取缓存Cache信息
	 */
	public CacheEntry getCache() {
		return mCache;
	}

	/**
	 * 设置缓存Cache信息,当实施缓存控制策略时生效
	 */
	public void setCache(CacheEntry cache) {
		if (CommonUtil.isEmpty(cache)) {
			L.i(TAG, "setCache(CacheEntry) : not allowed to set a null CacheEntry object");
			return;
		}
		mCache = cache;
	}

	/**
	 * 获取请求返回的响应回调函数,无则为null
	 */
	public ResponseCallback getCallback() {
		return mCallback;
	}

	/**
	 * 设置请求响应回调函数
	 */
	public void setCallback(ResponseCallback callback) {
		if (CommonUtil.isEmpty(callback)) {
			L.i(TAG, "setCallback(ResponseCallback) : not allowed to set a null ResponseCallback object");
			return;
		}
		mCallback = callback;
	}

	/**
	 * 是否已经进行了分发
	 */
	public boolean isDelivery() {
		return mIsDelivery;
	}

	/**
	 * 标记已经进行了分发
	 */
	public void markDeliveried() {
		mIsDelivery = true;
	}

	@Override
	public int compareTo(HttpRequest another) {
		return this.getPriority() == another.getPriority() ? this.mSequence - another.mSequence : another.getPriority
				().ordinal() - this.getPriority().ordinal();
	}

	/**
	 * 根据url和请求类型获取请求的键值
	 */
	public String getRequestKey() {
		if (mMethod.equals(Method.DELETE) || mMethod.equals(Method.GET)) {
			byte[] bUrl = getUrl().getBytes();
			byte[] bParam = getOutputData();
			byte[] bs = new byte[bUrl.length + bParam.length];
			CommonUtil.copy(bUrl, 0, bs, 0, bUrl.length);
			CommonUtil.copy(bParam, 0, bs, bUrl.length, bParam.length);
			return MD5.digestInHex(bs);
		} else {
			return MD5.digestInHex((getUrl() + getContentType()).getBytes());
		}
	}

	/**
	 * 获取请求的优先级别
	 * <p>需要由子类对应实现<p/>
	 */
	public abstract Priority getPriority();

	/**
	 * 获取请求正文格式
	 * <p>需要由子类对应实现<p/>
	 */
	public abstract String getContentType();

	/**
	 * 获取传递过去的请求数据
	 * <p>需要由子类对应实现<p/>
	 */
	public abstract byte[] getOutputData();
}
