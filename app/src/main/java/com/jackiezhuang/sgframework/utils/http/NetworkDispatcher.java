package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IDelivery;
import com.jackiezhuang.sgframework.utils.http.itfc.IHttpWorker;

import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 网络请求调度器，用于从网络请求队列中获取请求，然后执行请求任务，再交由分发器交付UI线程进行结果回调处理
 *
 * <p></p>
 * Created by zsigui on 15-8-27.
 */
public class NetworkDispatcher extends Dispatcher {

	private PriorityBlockingQueue<HttpRequest> mNetworkQueue;
	private IDelivery mDelivery;
	private IHttpWorker mWorker;

	public NetworkDispatcher(PriorityBlockingQueue<HttpRequest> networkQueue) {
		mNetworkQueue = networkQueue;
	}

	@Override
	public void run() {
		mQuit = false;
		while (!mQuit) {
			HttpRequest request;
			try {
				request = mNetworkQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			try {
				if (request.isCanceled()) {
					// 任务取消
					continue;
				}

				HttpResponse response = mWorker.performRequest(request, null);

				if (request.shouldCache() && response.isModified()) {
					// 添加或者更新Cache数据
					CacheManager.INSTANCE.putEntry(CacheManager.generateKeyByUrl(request.getUrl()), request.getCache());
				}

				if (!CommonUtil.isEmpty(request.getCallback())) {
					request.getCallback().onFinished(response);
				}
			} catch (SGHttpException | IOException e) {
				e.printStackTrace();
			}
		}
	}

}
