package com.jackiezhuang.sgframework.utils.http.bean;

import java.util.Map;

/**
 * HTTP请求执行后返回的数据实体类
 * <p/>
 * <p></p>
 * Created by zsigui on 15-8-21.
 */
public class HttpResponse {


	private byte[] mBodyContent;
	private String mParsedEncoding;
	private Map<String, String> mHeaders;
	private int mStatusCode;
	private boolean mIsModified = false;
	private boolean mIsSuccess = false;

	public HttpResponse(){}

	public HttpResponse(byte[] bodyContent, Map<String, String> headers, String parsedEncoding, int statusCode) {
		this(bodyContent, headers, parsedEncoding, statusCode, false);
	}

	public HttpResponse(byte[] bodyContent, Map<String, String> headers, String parsedEncoding, int statusCode,
	                    boolean isModified) {
		mBodyContent = bodyContent;
		mParsedEncoding = parsedEncoding;
		mHeaders = headers;
		mStatusCode = statusCode;
		mIsModified = isModified;
	}

	/**
	 * 获取Http请求后响应正文内容数据
	 */
	public byte[] getBodyContent() {
		return mBodyContent;
	}

	/**
	 * 设置Http请求后响应正文内容数据
	 */
	public void setBodyContent(byte[] bodyContent) {
		mBodyContent = bodyContent;
	}

	/**
	 * 获取正文格式编码
	 */
	public String getParsedEncoding() {
		return mParsedEncoding;
	}

	/**
	 * 设置正文格式编码
	 */
	public void setParsedEncoding(String parsedEncoding) {
		mParsedEncoding = parsedEncoding;
	}

	/**
	 * 获取Http响应头
	 */
	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	/**
	 * 设置Http响应头
	 */
	public void setHeaders(Map<String, String> headers) {
		mHeaders = headers;
	}

	/**
	 * 判断数据是否由更新(仅在使用缓存控制策略时起作用)
	 */
	public boolean isModified() {
		return mIsModified;
	}

	/**
	 * 设置数据是否被修改了
	 */
	public void setModified(boolean isModified) {
		mIsModified = isModified;
	}

	/**
	 * 获取Http请求的状态值
	 */
	public int getStatusCode() {
		return mStatusCode;
	}

	/**
	 * 设置Http请求状态值
	 */
	public void setStatusCode(int statusCode) {
		mStatusCode = statusCode;
	}

	/**
	 * 判断Http请求是否执行成功
	 */
	public boolean isSuccess() {
		return mIsSuccess;
	}

	/**
	 * 设置Http请求执行结果状态
	 */
	public void setSuccess(boolean isSuccess) {
		mIsSuccess = isSuccess;
	}
}
