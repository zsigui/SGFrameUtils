package com.jackiezhuang.sgframework.utils.http.bean;

/**
 * Created by zsigui on 15-8-24.
 */
public class CacheEntry extends CacheItem {

	private byte[] mData;

	public CacheEntry() {}

	public CacheEntry(CacheHeader entry, byte[] data) {
		mData = data;
		setEtag(entry.getEtag());
		setExpireTime(entry.getExpireTime());
		setServerTime(entry.getServerTime());
		setLastModifiedTime(entry.getLastModifiedTime());
		setResponseHeaders(entry.getResponseHeaders());
		setParsedEncoding(entry.getParsedEncoding());
	}

	public byte[] getData() {
		return mData;
	}

	public void setData(byte[] data) {
		mData = data;
	}

}
