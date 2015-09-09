package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.itfc.IManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zsigui on 15-8-24.
 */
public enum HttpManager implements IManager {

	INSTANCE;

	private static final String TAG = HttpManager.class.getName();

	// 等待执行请求缓冲区
	private final Map<String, Queue<HttpRequest>> mWaitingRequests;
	// 请求的序列化生成器,用于排序
	private AtomicInteger mSequenceGenerator;
	// 当前正在执行的请求集合
	private final Set<HttpRequest> mWorkRequests;
	// 需要执行网络请求的队列
	private PriorityBlockingQueue<HttpRequest> mNetworkQueue;
	private NetworkDispatcher[] mNetworkDispatcher;

	private HttpManager() {
		mWaitingRequests = new HashMap<>();
		mSequenceGenerator = new AtomicInteger();
		mWorkRequests = new HashSet<>();
		mNetworkQueue = new PriorityBlockingQueue<>();
	}

	@Override
	public void start() {
		stop();
		if (HttpConfig.sNeedCacheControl) {
			CacheManager.INSTANCE.start();
			CacheManager.INSTANCE.setNetworkQueue(mNetworkQueue);
		}
		if (CommonUtil.isEmpty(mNetworkDispatcher)) {
			mNetworkDispatcher = new NetworkDispatcher[HttpConfig.sHttpThreadCount];
			for (int i = 0; i < mNetworkDispatcher.length; i++) {
				mNetworkDispatcher[i] = new NetworkDispatcher(mNetworkQueue);
				mNetworkDispatcher[i].start();
			}
		}
	}

	@Override
	public void stop() {
		if (HttpConfig.sNeedCacheControl) {
			CacheManager.INSTANCE.stop();
		}
		if (!CommonUtil.isEmpty(mNetworkDispatcher)) {
			for (int i = 0; i < mNetworkDispatcher.length; i++) {
				mNetworkDispatcher[i].exit();
				mNetworkDispatcher[i] = null;
			}
		}
		mNetworkDispatcher = null;
	}

	@Override
	public void add(HttpRequest request) {
		if (CommonUtil.isEmpty(request) || request.isCanceled()) {
			L.i(TAG, "add(HttpRequest) : the request is null or canceled");
			return;
		}

		synchronized (mWorkRequests) {
			mWorkRequests.add(request);
		}

		// 设置请求的执行序列
		request.setSequence(mSequenceGenerator.incrementAndGet());

		if (!HttpConfig.sNeedCacheControl || !request.shouldCache()) {
			// 无须缓存，直接请求
			mNetworkQueue.add(request);
			return;
		}

		// 判断添加到等待队列，对于同一类型请求的后续直接使用Cache
		String requestKey = request.getRequestKey();
		synchronized (mWaitingRequests) {
			if (mWaitingRequests.containsKey(requestKey)) {
				Queue<HttpRequest> stagedRequests = mWaitingRequests.get(requestKey);
				if (stagedRequests == null) {
					stagedRequests = new LinkedList<>();
				}
				stagedRequests.add(request);
				mWaitingRequests.put(requestKey, stagedRequests);
			} else {
				mWaitingRequests.put(requestKey, null);
				CacheManager.INSTANCE.add(request);
			}
		}
	}

	/**
	 * 标记请求完成，并执行对应后续处理
	 */
	public void finished(HttpRequest request) {

		// 从工作队列中移除请求
		synchronized (mWorkRequests) {
			mWorkRequests.remove(request);
		}

		if (HttpConfig.sNeedCacheControl && request.shouldCache()) {
			// 使用缓存，将等待的同一类型请求都放到缓存管理器中去处理
			String requestKey = request.getRequestKey();
			synchronized (mWaitingRequests) {
				Queue<HttpRequest> waitingQueue = mWaitingRequests.get(requestKey);
				if (!CommonUtil.isEmpty(waitingQueue)) {
					L.d(TAG, String.format("finished(HttpRequest) : remain %s need to request from cache",
							waitingQueue.size()));
					CacheManager.INSTANCE.addAll(waitingQueue);
				}
			}
		}
	}

	@Override
	public void addAll(Collection<HttpRequest> requests) {
		if (CommonUtil.isEmpty(requests)) {
			L.i(TAG, "addAll(Collection<HttpRequest>) : the requests is null or empty so not allowed to be added");
			return;
		}
		synchronized (mWorkRequests) {
			for (HttpRequest request : requests) {
				if (!CommonUtil.isEmpty(request) && !request.isCanceled()) {
					mWorkRequests.add(request);
				}
			}
		}
	}

	@Override
	public void cancel(HttpRequest request) {
		if (mWorkRequests.contains(request)) {
			request.cancel();
		}
	}

	@Override
	public void cancelAll() {
		for (HttpRequest request : mWorkRequests) {
			request.cancel();
		}
	}

	@Override
	public void destroy() {
		cancelAll();
		mNetworkQueue.clear();
		stop();
	}
}
