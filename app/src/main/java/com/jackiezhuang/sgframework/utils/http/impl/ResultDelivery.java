package com.jackiezhuang.sgframework.utils.http.impl;

import android.os.Handler;

import com.jackiezhuang.sgframework.utils.http.HttpManager;
import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IDelivery;

/**
 * Created by zsigui on 15-9-1.
 */
public class ResultDelivery implements IDelivery {

	private Handler mHandler;

	public ResultDelivery(Handler handler) {
		mHandler = handler;
	}

	@Override
	public void postResponse(final HttpRequest request, final HttpResponse response) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (request.isCanceled()) {
					// 已经取消
					return;
				}
				HttpManager.INSTANCE.finished(request);
				request.markDeliveried();
				request.getCallback().onFinished(response, null);
			}
		});
	}

	@Override
	public void postError(final HttpRequest request, final SGHttpException error) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (request.isCanceled()) {
					// 已经取消
					return;
				}
				HttpManager.INSTANCE.finished(request);
				request.markDeliveried();
				request.getCallback().onFinished(null, error);
			}
		});
	}

	@Override
	public void postDownloadProgress(HttpRequest request, long totalSize, long currentSize) {
		// 暂不处理
	}
}
