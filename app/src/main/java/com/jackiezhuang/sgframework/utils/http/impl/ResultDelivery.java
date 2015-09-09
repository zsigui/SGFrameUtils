package com.jackiezhuang.sgframework.utils.http.impl;

import android.os.Handler;
import android.os.Looper;

import com.jackiezhuang.sgframework.utils.http.HttpManager;
import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.DownloadRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IDelivery;

/**
 * 结果通知回调分发器，可以根据需要传入响应处理的Handler，默认分发到主线程Handler执行
 *
 * Created by zsigui on 15-9-1.
 */
public class ResultDelivery implements IDelivery {

	private Handler mHandler;

	/**
	 * 默认构造函数，构建UI线程回调响应的分发器
	 */
	public ResultDelivery() { this(new Handler(Looper.getMainLooper())); }

	/**
	 * 传入分发器的响应处理Handler
	 */
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
				request.markDelivered();
				request.getCallback().onSuccess(response);
				request.getCallback().onFinished();
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
				request.markDelivered();
				request.getCallback().onFailure(error);
				request.getCallback().onFinished();
			}
		});
	}

	@Override
	public void postDownloadProgress(final HttpRequest request, final long totalSize, final long currentSize) {
		// 暂不处理
		if (request instanceof DownloadRequest) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					request.getCallback().onDownload(currentSize, totalSize);
				}
			});
		}
	}
}
