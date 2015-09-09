package com.jackiezhuang.sgframework.utils.http.bean;

/**
 * Created by zsigui on 15-8-24.
 */
public class CacheHeader extends CacheItem {

	private String mKey;
	private long mDataSize;

	public CacheHeader() {
	}

	public CacheHeader(String key, CacheEntry entry) {
		mKey = key;
		mDataSize = entry.getData().length;
		setEtag(entry.getEtag());
		setExpireTime(entry.getExpireTime());
		setServerTime(entry.getServerTime());
		setLastModifiedTime(entry.getLastModifiedTime());
		setResponseHeaders(entry.getResponseHeaders());
		setParsedEncoding(entry.getParsedEncoding());
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

	public boolean isExpired() {
		return getExpireTime() < System.currentTimeMillis();
	}
}
