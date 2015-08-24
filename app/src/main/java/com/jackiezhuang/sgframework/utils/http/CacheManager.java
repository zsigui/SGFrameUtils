package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.chiper.MD5;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.CacheEntry;
import com.jackiezhuang.sgframework.utils.http.bean.CacheHeader;
import com.jackiezhuang.sgframework.utils.io.FileUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache缓存的管理类(使用enum实现),执行读取/写入/刷新缓存等操作
 *
 * <p></p>
 * Created by zsigui on 15-8-24.
 */
public enum CacheManager {

	INSTANCE;
	public static final String TAG = CacheManager.class.getName();

	/**
	 * 存放缓存数据
	 */
	private Map<String, CacheHeader> mCacheMap = new HashMap<>(16, 0.75F);
	/**
	 * 指定磁盘缓存大小
	 */
	private long mTotalCacheSize = 5 * 1024 * 1024;
	private String mCacheDirPath;

	public CacheHeader getHeader(String key) {
		return mCacheMap.get(key);
	}

	public CacheEntry getEntry(String key) {
		if (!mCacheMap.containsKey(key)) {
			return null;
		}
		CacheEntry entry = new CacheEntry(mCacheMap.get(key));

		return entry;
	}

	public synchronized void put(String key, CacheEntry entry) {
		if (CommonUtil.isEmpty(entry) || CommonUtil.isEmpty(key)) {
			L.e(TAG, "add : key and entry is not allowed to be null");
			return;
		}
		if (mCacheMap.containsKey(key)) {
			mTotalCacheSize += (entry.getData().length - mCacheMap.get(key).getDataSize());
		} else {
			mTotalCacheSize += entry.getData().length;
		}
		mCacheMap.put(key, new CacheHeader(entry));
	}

	public synchronized void put(String key, CacheHeader entry) {
		if (CommonUtil.isEmpty(entry) || CommonUtil.isEmpty(key)) {
			L.e(TAG, "put : key and entry is not allowed to be null");
			return;
		}

	}

	/**
	 * 清除所有缓存
	 */
	public synchronized void clear() {
		FileUtil.delete(mCacheDirPath);
		mCacheMap.clear();
		mTotalCacheSize = 0;
	}

	/**
	 * 移除指定缓存
	 *
	 * @param key 查找键值
	 */
	public synchronized void remove(String key) {
		if (CommonUtil.isEmpty(key)) {
			L.e(TAG, "isEmpty : key is not allowed to be null");
			return;
		}
		if (!mCacheMap.containsKey(key)) {
			L.i(TAG, "remove : key is not found");
			return;
		}
		FileUtil.delete(getCacheFilePath(key));
		mTotalCacheSize -= mCacheMap.remove(key).getDataSize();
	}

	private String getCacheFilePath(String key) {
		return FileUtil.getAbsolutePath(String.format("%s/%s", mCacheDirPath, getFilenameByKey(key)));
	}

	private String getFilenameByKey(String key) {
		return MD5.digestInBase64(key, SGConfig.DEFAULT_SYS_CHARSET);
	}
}
