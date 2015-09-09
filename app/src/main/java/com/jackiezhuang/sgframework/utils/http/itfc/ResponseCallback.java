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
	 * 完成网络请求后不论成功与否都会回调
	 */
	public void onFinished(){};

	/**
	 * 执行网络请求成功后回调
	 */
	public void onSuccess(HttpResponse response){};

	/**
	 * 执行网络请求失败后回调
	 */
	public void onFailure(SGHttpException error){};

	/**
	 * 执行下载网络请求后的下载进度回调
	 * <p>注意：该方法只会响应下载请求<p/>
	 * @param currentSize
	 * @param totalSize
	 */
	public void onDownload(long currentSize, long totalSize){};
}
