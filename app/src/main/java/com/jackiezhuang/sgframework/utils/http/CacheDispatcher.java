package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.CacheEntry;
import com.jackiezhuang.sgframework.utils.http.bean.CacheHeader;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IDelivery;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * 缓存调度线程,负责Cache执行请求调度
 * <p/>
 * <p><p/>
 * Created by zsigui on 15-8-26.
 */
public class CacheDispatcher extends Dispatcher {

	private CacheDiskController mCacheController;
	private PriorityBlockingQueue<HttpRequest> mCacheQueue;
	private PriorityBlockingQueue<HttpRequest> mNetworkQueue;
	private IDelivery mDelivery = null;

	public CacheDispatcher(CacheDiskController cacheController, PriorityBlockingQueue<HttpRequest> cacheQueue,
	                       PriorityBlockingQueue<HttpRequest>
			networkQueue) {
		mCacheController = cacheController;
		mCacheQueue = cacheQueue;
		mNetworkQueue = networkQueue;
	}

	@Override
	public void run() {
		mCacheController.init();
		mQuit = false;
		while (!mQuit) {
			try {
				final HttpRequest request = mCacheQueue.take();
				CacheHeader cache = mCacheController.getHeader(request.getUrl());
				if (CommonUtil.isEmpty(cache)) {
					// 不存在该缓存,需要进行网络请求
					mNetworkQueue.add(request);
					return;
				}

				if (cache.isExpired()) {
					// 请求过期,需要重新进行网络请求刷新缓存数据
					request.setCache(new CacheEntry(cache, null));
					mNetworkQueue.add(request);
				}

				HttpResponse response = new HttpResponse(mCacheController.getData(cache.getKey()),
						cache.getResponseheaders(), null, 304, false);
				response.setSuccess(true);

				if (HttpConfig.sDelayCache) {
					sleep(HttpConfig.sDelayTime);
				}

				mDelivery.postResponse(request, response);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
