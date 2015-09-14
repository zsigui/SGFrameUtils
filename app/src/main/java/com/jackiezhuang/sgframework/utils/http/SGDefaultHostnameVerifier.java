package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.L;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by zsigui on 15-8-28.
 */
public class SGDefaultHostnameVerifier implements HostnameVerifier {

	private static final String TAG = SGDefaultHostnameVerifier.class.toString();

	@Override
	public boolean verify(String hostname, SSLSession session) {
		L.i(TAG, "verify(String, SSLSession) : doesn't do any verify in the default config");
		return true;
	}
}
