package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.http.bean.FileRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IDelivery;
import com.jackiezhuang.sgframework.utils.http.itfc.IHttpAction;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by zsigui on 15-8-27.
 */
public class NetworkDispatcher extends Dispatcher {

	private PriorityBlockingQueue<HttpRequest> mNetworkQueue;
	private IDelivery mDelivery;

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
			if (request.isCanceled()) {
				// 任务取消
				continue;
			}

			HttpResponse response = performRequest(request);
		}
	}

	private HttpResponse performRequest(final HttpRequest request) {
		HttpResponse result = null;
		HttpUtil.requestForBytesResponse(request.getUrl(), new IHttpAction<byte[]>() {
			@Override
			public void beforeConnect(HttpURLConnection urlConnection) throws IOException {
				urlConnection.setRequestMethod(request.getMethod());
				urlConnection.setDoInput(true);
				for (Map.Entry<String, String> header : request.getRequestHeaders().entrySet()) {
					urlConnection.setRequestProperty(header.getKey(), header.getValue());
				}
			}

			@Override
			public void afterConnect(HttpURLConnection urlConnection) throws IOException {

				if (request instanceof FileRequest) {

				}
			}

			@Override
			public byte[] onResponse(HttpResponse response) {
				return null;
			}
		});
		return result;
	}

}
