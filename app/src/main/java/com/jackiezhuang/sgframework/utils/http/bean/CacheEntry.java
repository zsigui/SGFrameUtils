package com.jackiezhuang.sgframework.utils.http.bean;

import java.util.Map;

/**
 * Created by zsigui on 15-8-24.
 */
public class CacheEntry {

	private String mEtag;
	private long mExpireTime;
	private long mServerTime;
	private long mLastModifiedTime;
	private Map<String, String> mResponseHeaders;
	private byte[] mData;

	public CacheEntry() {}

	public CacheEntry(CacheHeader header, byte[] data) {
		mEtag = header.getEtag();
		mExpireTime = header.getExpireTime();
		mServerTime = header.getServerTime();
		mLastModifiedTime = header.getLastModifiedTime();
		mData = data;
		mResponseHeaders = header.getResponseheaders();
	}

	public byte[] getData() {
		return mData;
	}

	public void setData(byte[] data) {
		mData = data;
	}

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

	public Map<String, String> getResponseHeaders() {
		return mResponseHeaders;
	}

	public void setResponseHeaders(Map<String, String> responseHeaders) {
		this.mResponseHeaders = responseHeaders;
	}
}
