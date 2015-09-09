package com.jackiezhuang.sgframework.utils.http;

import android.os.Process;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
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

	private static final String TAG = CacheDispatcher.class.getName();

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
		android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		mCacheController.init();
		mQuit = false;
		while (!mQuit) {
			HttpRequest request = null;
			try {
				request = mCacheQueue.take();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			try {
				if (request.isCanceled()) {
					L.i(TAG, String.format("run : request of url '%s' is canceled ", request.getUrl()));
					HttpManager.INSTANCE.finished(request);
					continue;
				}

				CacheHeader cache = mCacheController.getHeader(request.getRequestKey());
				if (CommonUtil.isEmpty(cache)) {
					// 不存在该缓存,需要进行网络请求
					mNetworkQueue.add(request);
					continue;
				}

				if (cache.isExpired()) {
					// 请求过期,需要重新进行网络请求刷新缓存数据
					request.setRequestHeaders(cache.getResponseHeaders());
					mNetworkQueue.add(request);
					continue;
				}

				HttpResponse response = new HttpResponse(mCacheController.getData(cache.getKey()),
						cache.getResponseHeaders(), cache.getParsedEncoding(), 304, false);
				response.setSuccess(true);

				if (HttpConfig.sDelayCache) {
					sleep(HttpConfig.sDelayTime);
				}

				mDelivery.postResponse(request, response);

			} catch (InterruptedException e) {
				e.printStackTrace();
				mDelivery.postError(request, new SGHttpException(e));
			}
		}
	}

}
