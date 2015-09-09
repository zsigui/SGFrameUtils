package com.jackiezhuang.sgframework.utils.http.bean;

import java.util.Map;

/**
 * Created by JackieZhuang on 2015/9/10.
 */
public abstract class CacheItem {
	private String mEtag;
	private long mExpireTime;
	private long mServerTime;
	private long mLastModifiedTime;
	private String mParsedEncoding;
	private Map<String, String> mResponseHeaders;

	public String getEtag() {
		return mEtag;
	}

	public void setEtag(String etag) {
		mEtag = etag;
	}

	public long getExpireTime() {
		return mExpireTime;
	}

	public void setExpireTime(long expireTime) {
		mExpireTime = expireTime;
	}

	public long getServerTime() {
		return mServerTime;
	}

	public void setServerTime(long serverTime) {
		mServerTime = serverTime;
	}

	public long getLastModifiedTime() {
		return mLastModifiedTime;
	}

	public void setLastModifiedTime(long lastModifiedTime) {
		mLastModifiedTime = lastModifiedTime;
	}

	public String getParsedEncoding() {
		return mParsedEncoding;
	}

	public void setParsedEncoding(String parsedEncoding) {
		mParsedEncoding = parsedEncoding;
	}

	public Map<String, String> getResponseHeaders() {
		return mResponseHeaders;
	}

	public void setResponseHeaders(Map<String, String> responseHeaders) {
		mResponseHeaders = responseHeaders;
	}
}
