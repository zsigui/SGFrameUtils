package com.jackiezhuang.sgframework.utils.http.itfc;

import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;

/**
 * 分发器,用于Http请求返回结果的分发处理
 * <p></p>
 * Created by zsigui on 15-8-27.
 */
public interface IDelivery {

	void postResponse(HttpRequest request, HttpResponse response);

	void postError(HttpRequest request, SGHttpException error);

	void postDownloadProgress(HttpRequest request, long totalSize, long currentSize);
}
