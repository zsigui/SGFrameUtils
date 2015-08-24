package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zsigui on 15-8-24.
 */
public class HttpManager {

	// 等待执行请求缓冲区
	private final Map<String, Queue<HttpRequest>> mWaitingRequests = new HashMap<>();
	// 请求的序列化生成器
	private final AtomicInteger mSequenceGenertor = new AtomicInteger();
	// 当前正在执行的请求集合
	private final Set<HttpRequest> mWorkingRequests = new HashSet<>();

}
