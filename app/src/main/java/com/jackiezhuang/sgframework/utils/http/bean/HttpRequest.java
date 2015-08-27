package com.jackiezhuang.sgframework.utils.http.bean;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;

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
	private boolean mDeleyCache;
	private int mDelayTime;

	/**
	 * 请求优先级序列值
	 */
	private int mSequence;

	/**
	 * 获取请求地址
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * 设置请求地址,同时会对地址进行简单的头协议检查,默认设置为http(若为https,需要自行配置)
	 */
	public void setUrl(String url) {
		if (CommonUtil.isEmpty(url)) {
			L.e(TAG, "setUrl : url is not allowed to be null or \"\"");
			//throw new IllegalArgumentException(TAG + ".setUrl : mUrl is not allowed to be null or \"\"");
			return;
		}
		if (!url.startsWith("http://") || !url.startsWith("https://")) {
			url = "http://" + url;
		}
		this.mUrl = url;
	}

	/**
	 * 获取请求方法名
	 */
	public String getMethod() {
		return mMethod.val();
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

	public boolean isDeleyCache() {
		return mDeleyCache;
	}

	public void setDeleyCache(boolean deleyCache) {
		mDeleyCache = deleyCache;
	}

	@Override
	public int compareTo(HttpRequest another) {
		return this.getPriority() == another.getPriority() ? this.mSequence - another.mSequence : another.getPriority
				().ordinal() - this.getPriority().ordinal();
	}

	/**
	 * 获取请求的优先级别
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
