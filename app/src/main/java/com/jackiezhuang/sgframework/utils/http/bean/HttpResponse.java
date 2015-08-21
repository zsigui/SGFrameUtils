package com.jackiezhuang.sgframework.utils.http.bean;

import java.util.Map;

/**
 * HTTP请求执行后返回的数据实体类
 *
 * <p></p>
 * Created by zsigui on 15-8-21.
 */
public class HttpResponse {

	private byte[] mBodyContent;
	private String mParsedEncoding;
	private Map<String, String> mHeaders;
	private int mStatusCode;
	private boolean mIsModified = false;

	public HttpResponse() {}

	public HttpResponse(byte[] bodyContent, Map<String, String> headers, String parsedEncoding, int statusCode,
	                    boolean isModified) {
		mBodyContent = bodyContent;
		mParsedEncoding = parsedEncoding;
		mHeaders = headers;
		mStatusCode = statusCode;
		mIsModified = isModified;
	}

	public HttpResponse(byte[] bodyContent, String parsedEncoding, Map<String, String> headers, int statusCode) {
		mBodyContent = bodyContent;
		mParsedEncoding = parsedEncoding;
		mHeaders = headers;
		mStatusCode = statusCode;
	}

	public byte[] getBodyContent() {
		return mBodyContent;
	}

	public void setBodyContent(byte[] bodyContent) {
		mBodyContent = bodyContent;
	}

	public String getParsedEncoding() {
		return mParsedEncoding;
	}

	public void setParsedEncoding(String parsedEncoding) {
		mParsedEncoding = parsedEncoding;
	}

	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	public void setHeaders(Map<String, String> headers) {
		mHeaders = headers;
	}

	public boolean isModified() {
		return mIsModified;
	}

	public void setModified(boolean isModified) {
		mIsModified = isModified;
	}

	public int getStatusCode() {
		return mStatusCode;
	}

	public void setStatusCode(int statusCode) {
		mStatusCode = statusCode;
	}
}
