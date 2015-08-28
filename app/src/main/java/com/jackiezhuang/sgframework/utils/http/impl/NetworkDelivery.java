package com.jackiezhuang.sgframework.utils.http.impl;

import android.os.Handler;
import android.os.Looper;

import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IDelivery;

/**
 * Created by zsigui on 15-8-28.
 */
public class NetworkDelivery implements IDelivery{

	/**
	 * 主线程处理器
	 */
	private Handler mHandler = new Handler(Looper.getMainLooper());

	@Override
	public void postResponse(final HttpRequest request, HttpResponse response) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {

			}
		});
	}

	@Override
	public void postError(HttpRequest request, SGHttpException error) {

	}

	@Override
	public void postDownloadProgress(HttpRequest request, long totalSize, long currentSize) {

	}
}
