package com.jackiezhuang.sgframework.utils.http.itfc;

import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;

/**
 * Http请求结果的响应回调接口
 * <p><p/>
 * Created by zsigui on 15-8-28.
 */
public abstract class ResponseCallback {

	/**
	 * 完成网络请求后不论成功与否都会回调，该方法执行在UI主线程中
	 */
	public void onFinished(){};

	/**
	 * 执行网络请求成功后回调，该方法执行在UI主线程中
	 */
	public void onSuccess(HttpResponse response){};

	/**
	 * 执行网络请求失败后回调，该方法执行在UI主线程中
	 */
	public void onFailure(SGHttpException error){};
}
