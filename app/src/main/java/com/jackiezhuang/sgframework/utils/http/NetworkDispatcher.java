package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.CacheHeader;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IDelivery;
import com.jackiezhuang.sgframework.utils.http.itfc.IHttpWorker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 网络请求调度器，用于从网络请求队列中获取请求，然后执行请求任务，再交由分发器交付UI线程进行结果回调处理
 * <p/>
 * <p></p>
 * Created by zsigui on 15-8-27.
 */
public class NetworkDispatcher extends Dispatcher {

	private PriorityBlockingQueue<HttpRequest> mNetworkQueue;
	private IDelivery mDelivery;
	private IHttpWorker mWorker;

	public NetworkDispatcher(PriorityBlockingQueue<HttpRequest> networkQueue) {
		mNetworkQueue = networkQueue;
		mWorker = HttpConfig.sWorker;
		mDelivery = HttpConfig.sDelivery;
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
					// 任务取消，通知完成
					HttpManager.INSTANCE.finished(request);
					continue;
				}

				// 执行请求
				HttpResponse response = performRequest(request);

				if (!response.isModified() && request.isDelivery()) {
					// 网络请求结果未变化且该请求已经分发过，无须再处理
					HttpManager.INSTANCE.finished(request);
					continue;
				}

				if (HttpConfig.sNeedCache && request.shouldCache() && response.isModified()) {
					// 添加或者更新Cache数据
					CacheManager.INSTANCE.putEntry(request.getRequestKey(), request.getCache());
				}

				if (response.isSuccess()) {
					mDelivery.postResponse(request, response);
				} else {
					mDelivery.postError(request, new SGHttpException(new String(response.getBodyContent(),
							response.getParsedEncoding())));
				}
			} catch (SGHttpException e) {
				e.printStackTrace();
				mDelivery.postError(request, e);
			} catch (IOException e) {
				e.printStackTrace();
				mDelivery.postError(request, new SGHttpException(e));
			}
		}
	}

	private HttpResponse performRequest(HttpRequest request) throws IOException, SGHttpException {
		Map<String, String> additionHeaders = null;
		CacheHeader header = CacheManager.INSTANCE.getEntryHeader(request.getRequestKey());
		if (!CommonUtil.isEmpty(header)) {
			additionHeaders = new HashMap<>();
			if (!CommonUtil.isEmpty(header.getEtag())) {
				additionHeaders.put("If-None-Match", header.getEtag());
			}
			if (header.getServerTime() > 0) {
				additionHeaders.put("If-Modified-Since", "");
			}
		}
		return mWorker.performRequest(request, additionHeaders);
	}

}
