package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zsigui on 15-8-24.
 */
public class HttpManager {


	// 等待执行请求缓冲区
	private final Map<String, Queue<HttpRequest>> mWaitingRequests = new HashMap<>();
	// 请求的序列化生成器
	private final AtomicInteger mSequenceGenertor = new AtomicInteger();
	// 当前正在执行的请求集合
	private final Set<HttpRequest> mWorkingRequests = new HashSet<>();
	// 需要执行网络请求的队列
	private final PriorityBlockingQueue<HttpRequest> mNetworkQueue = new PriorityBlockingQueue<>();
	// 判断是否进行是初始化设置
	private volatile boolean mIsInit = false;

	private static class SingletonHolder {
		private static final HttpManager INSTANCE = new HttpManager();
	}


	private HttpManager(){}

	private void init() {

	}

	private HttpManager getInstance() {
		if (!mIsInit) {
			synchronized (SingletonHolder.INSTANCE) {
				if (!mIsInit) {
					init();
					mIsInit = true;
				}
			}
		}
		return SingletonHolder.INSTANCE;
	}
}
