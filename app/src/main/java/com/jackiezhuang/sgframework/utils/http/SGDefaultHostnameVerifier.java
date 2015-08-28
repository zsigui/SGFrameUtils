package com.jackiezhuang.sgframework.utils.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by zsigui on 15-8-28.
 */
public class SGDefaultHostnameVerifier implements HostnameVerifier {
	@Override
	public boolean verify(String hostname, SSLSession session) {

		return true;
	}
}
