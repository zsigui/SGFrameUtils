package com.jackiezhuang.sgframework.utils.http.bean;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;

/**
 * Created by zsigui on 15-8-24.
 */
public abstract class HttpRequest {

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


	private String mUrl = null;
	private Method mMethod = Method.GET;

	public String getUrl() {
		return mUrl;
	}

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

	public String getMethod() {
		return mMethod.val();
	}

	public void setMethod(Method method) {
		if (CommonUtil.isEmpty(method)) {
			//throw new IllegalArgumentException(TAG + ".setMethod : method is not allowed to be null");
			return;
		}
		this.mMethod = method;
	}

	public abstract String getContentType();

	public abstract byte[] getOutputData();
}
