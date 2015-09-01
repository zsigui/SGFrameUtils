package com.jackiezhuang.sgframework.utils.http.itfc;

import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;

/**
 * Http请求结果的响应回调接口
 * <p><p/>
 * Created by zsigui on 15-8-28.
 */
public interface ResponseCallback {

	/**
	 * 完成请求的结果回调，该方法执行在UI主线程中
	 */
	void onFinished(HttpResponse response, SGHttpException error);
}
