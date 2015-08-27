package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;

/**
 * Created by zsigui on 15-8-27.
 */
public class SGHttpException extends Exception {
	public final HttpResponse networkResponse;

	public SGHttpException() {
		networkResponse = null;
	}

	public SGHttpException(HttpResponse response) {
		networkResponse = response;
	}

	public SGHttpException(String exceptionMessage) {
		super(exceptionMessage);
		networkResponse = null;
	}

	public SGHttpException(String exceptionMessage, HttpResponse response) {
		super(exceptionMessage);
		networkResponse = response;
	}

	public SGHttpException(String exceptionMessage, Throwable reason) {
		super(exceptionMessage, reason);
		networkResponse = null;
	}

	public SGHttpException(Throwable cause) {
		super(cause);
		networkResponse = null;
	}
}