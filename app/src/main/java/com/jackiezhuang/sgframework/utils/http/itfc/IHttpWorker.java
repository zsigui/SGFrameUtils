package com.jackiezhuang.sgframework.utils.http.itfc;

import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.NetworkResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zsigui on 15-8-28.
 */
public interface IHttpWorker {

	NetworkResponse performRequest(HttpRequest request, Map<String, String> additionalHeaders) throws IOException, SGHttpException;
}
