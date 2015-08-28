package com.jackiezhuang.sgframework.utils.http.itfc;

import com.jackiezhuang.sgframework.utils.http.SGHttpException;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zsigui on 15-8-28.
 */
public interface IHttpStack {

	HttpResponse performRequest(HttpRequest request, Map<String, String> additionalHeaders) throws IOException, SGHttpException;
}
