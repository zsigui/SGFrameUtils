package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.chiper.MD5;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.CacheEntry;
import com.jackiezhuang.sgframework.utils.http.bean.CacheHeader;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.itfc.IManager;

import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 缓存管理操作类,负责Cache请求的添加管理
 *
 * <p></p>
 * Created by zsigui on 15-8-27.
 */
public enum CacheManager implements IManager{

	INSTANCE;

	private static final String TAG = CacheManager.class.getName();

	private static final int DISPACHTER_COUNT = 2;
	// 需要执行缓存请求的队列
	private PriorityBlockingQueue<HttpRequest> mCacheQueue = new PriorityBlockingQueue<>();
	// 需要执行网络请求的队列,需要由HttpManager调用设置
	private PriorityBlockingQueue<HttpRequest> mNetworkQueue;
	private CacheDispatcher[] mDispatchers;
	private CacheDiskController mController;

	public void setNetworkQueue(PriorityBlockingQueue<HttpRequest> networkQueue) {
		mNetworkQueue = networkQueue;
	}

	public void addCacheRequest(HttpRequest request) {
		mCacheQueue.add(request);
	}

	public HttpRequest getCacheRequest(HttpRequest request) {
		try {
			return mCacheQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void putEntry(String key, CacheEntry entry) {
		mController.put(key, entry);
	}

	public void putEntryHeader(String key, CacheHeader entry) {
		mController.put(key, entry);
	}

	public CacheEntry getEntry(String key) {
		return mController.getEntry(key);
	}

	public CacheHeader getEntryHeader(String key) {
		return mController.getHeader(key);
	}

	public byte[] getEntryData(String key) {
		return mController.getData(key);
	}

	public static String generateKeyByUrl(String url) {
		return MD5.digestInBase64(url, SGConfig.DEFAULT_UTF_CHARSET);
	}

	@Override
	public synchronized void start() {
		stop();
		if (mController == null) {
			mController = new CacheDiskController();
		}
		mDispatchers = new CacheDispatcher[2];
		for (int i = 0; i < mDispatchers.length; i++) {
			mDispatchers[i] = new CacheDispatcher(mController, mCacheQueue, mNetworkQueue);
			mDispatchers[i].start();
		}

	}

	@Override
	public synchronized void stop() {
		if (mDispatchers != null) {
			for (int i = 0; i < mDispatchers.length; i++) {
				mDispatchers[i].exit();
			}
		}
		mDispatchers = null;
	}

	@Override
	public void add(HttpRequest request) {
		if (CommonUtil.isEmpty(request)) {
			L.e(TAG, "add(HttpRequest) : can't add a null request");
			return;
		}
		mCacheQueue.add(request);
	}

	@Override
	public void addAll(Collection<HttpRequest> requests) {
		if (CommonUtil.isEmpty(requests)) {
			L.e(TAG, "addAll(Collection<HttpRequest>) : no allowed type request add");
			return;
		}
		mCacheQueue.addAll(requests);
	}

	@Override
	public void cancel(HttpRequest request) {
		if (mCacheQueue.contains(request))
			request.cancel();
	}

	@Override
	public void cancelAll() {
		for (HttpRequest request : mCacheQueue) {
			request.cancel();
		}
	}

	@Override
	public void destroy() {
		cancelAll();
		stop();
	}
}
