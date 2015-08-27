package com.jackiezhuang.sgframework.utils.http.bean;

import java.util.Map;

/**
 * Created by zsigui on 15-8-24.
 */
public class CacheHeader {

	private String mEtag;
	private String mKey;
	private long mExpireTime;
	private long mServerTime;
	private long mLastModifiedTime;
	private long mDataSize;
	private Map<String, String> mResponseheaders;

	public CacheHeader() {
	}

	public CacheHeader(String key, CacheEntry entry) {
		mKey = key;
		mEtag = entry.getEtag();
		mExpireTime = entry.getExpireTime();
		mServerTime = entry.getServerTime();
		mLastModifiedTime = entry.getLastModifiedTime();
		mDataSize = entry.getData().length;
		mResponseheaders = entry.getResponseHeaders();
	}

	public String getKey() {
		return mKey;
	}

	public void setKey(String key) {
		mKey = key;
	}

	public long getDataSize() {
		return mDataSize;
	}

	public void setDataSize(long dataSize) {
		mDataSize = dataSize;
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

	public Map<String, String> getResponseheaders() {
		return mResponseheaders;
	}

	public void setResponseheaders(Map<String, String> responseheaders) {
		this.mResponseheaders = responseheaders;
	}

	public void setLastModifiedTime(long lastModifiedTime) {
		mLastModifiedTime = lastModifiedTime;
	}

	public boolean isExpired() {
		return this.mExpireTime < System.currentTimeMillis();
	}
}
