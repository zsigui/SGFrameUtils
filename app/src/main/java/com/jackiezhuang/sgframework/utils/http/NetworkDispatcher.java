package com.jackiezhuang.sgframework.utils.http;

import android.os.Process;

import com.jackiezhuang.sgframework.utils.DateUtil;
import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.CacheHeader;
import com.jackiezhuang.sgframework.utils.http.bean.DownloadRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpRequest;
import com.jackiezhuang.sgframework.utils.http.bean.HttpResponse;
import com.jackiezhuang.sgframework.utils.http.bean.NetworkResponse;
import com.jackiezhuang.sgframework.utils.http.itfc.IDelivery;
import com.jackiezhuang.sgframework.utils.http.itfc.IHttpWorker;
import com.jackiezhuang.sgframework.utils.io.IOUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 网络请求调度器，用于从网络请求队列中获取请求，然后执行请求任务，再交由分发器交付UI线程进行结果回调处理
 * <p/>
 * <p></p>
 * Created by zsigui on 15-8-27.
 */
public class NetworkDispatcher extends Dispatcher {

	private PriorityBlockingQueue<HttpRequest> mNetworkQueue;
	private IDelivery mDelivery;
	private IHttpWorker mWorker;

	public NetworkDispatcher(PriorityBlockingQueue<HttpRequest> networkQueue) {
		mNetworkQueue = networkQueue;
		mWorker = HttpConfig.sWorker;
		mDelivery = HttpConfig.sDelivery;
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		mQuit = false;
		while (!mQuit) {
			HttpRequest request;
			try {
				request = mNetworkQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			try {
				if (request.isCanceled()) {
					// 任务取消，通知完成
					HttpManager.INSTANCE.finished(request);
					continue;
				}

				// 执行请求
				HttpResponse response = performRequest(request);

				if (request instanceof DownloadRequest) {
					continue;
				}

				if (!response.isModified() && request.isDelivery()) {
					// 网络请求结果未变化且该请求已经分发过，无须再处理
					HttpManager.INSTANCE.finished(request);
					continue;
				}

				if (response.isSuccess()) {
					mDelivery.postResponse(request, response);
				} else {
					mDelivery.postError(request, new SGHttpException(new String(response.getBodyContent(),
							response.getParsedEncoding())));
				}
			} catch (SGHttpException e) {
				e.printStackTrace();
				mDelivery.postError(request, e);
			} catch (IOException e) {
				e.printStackTrace();
				mDelivery.postError(request, new SGHttpException(e));
			}
		}
	}

	/**
	 * 执行网络请求并返回结果
	 */
	private HttpResponse performRequest(HttpRequest request) throws IOException, SGHttpException {
		addCacheHeader(request);
		NetworkResponse response = mWorker.performRequest(request);
		HttpResponse result = null;
		if (request instanceof DownloadRequest && response.getResponseCode() == HttpURLConnection.HTTP_OK) {
			// 对于下载文件的请求,交由其它控制程序处理
			((DownloadRequest) request).handleRespContent(response);
		} else {
			// 非下载文件请求，解析并返回结果
			result = new HttpResponse();
			result.setModified(!(response.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED));
			result.setSuccess(response.getResponseCode() == HttpURLConnection.HTTP_OK || response.getResponseCode() ==
					HttpURLConnection.HTTP_NOT_MODIFIED);
			result.setStatusCode(response.getResponseCode());
			result.setParsedEncoding(HttpUtil.parseCharset(response.getContentType(), result.getBodyContent(),
					SGConfig.DEFAULT_ISO_CHARSET));
			result.setHeaders(response.getHeaders());
			result.setBodyContent(IOUtil.readBytes(response.getContent()));
			if (HttpConfig.sNeedCacheControl && request.shouldCache() && result.isModified()) {
				// 写入缓存
				CacheManager.INSTANCE.putEntry(request.getRequestKey(), HttpUtil.parseResponseHeader(response));
			}
		}
		return result;
	}

	/**
	 * 添加额外的缓存头信息
	 */
	private void addCacheHeader(HttpRequest request) {
		if (HttpConfig.sUseCacheHeader) {
			CacheHeader header = CacheManager.INSTANCE.getEntryHeader(request.getRequestKey());
			Map<String, String> requestHeaders = request.getRequestHeaders();
			if (!CommonUtil.isEmpty(header)) {
				if (!CommonUtil.isEmpty(header.getEtag()) && !requestHeaders.containsKey("If-None-Match")) {
					requestHeaders.put("If-None-Match", header.getEtag());
				}
				if (header.getServerTime() > 0 && !requestHeaders.containsKey("If-Modified-Since")) {
					requestHeaders.put("If-Modified-Since", DateUtil.formatGMTDate(DateUtil.getDate(header.getServerTime
							())));
				}
				if (!CommonUtil.isEmpty(header) && header.getResponseHeaders().containsKey("Cookie")) {
					String cacheCookie = header.getResponseHeaders().get("Cookie");
					// 设置Cookie头，去除重复的，使用新添加的替换Cache缓存的
					if (requestHeaders.containsKey("Cookie")) {
						Map<String, String> cacheCookieMap = getCookieMap(cacheCookie);
						cacheCookieMap.putAll(getCookieMap(requestHeaders.get("Cookie")));
						cacheCookie = "";
						for (Map.Entry<String, String> item : cacheCookieMap.entrySet()) {
							cacheCookie += item.getKey() + "=" + item.getValue() + ";";
						}
					}
					requestHeaders.put("Cookie", cacheCookie);
				}
			}
			request.setRequestHeaders(requestHeaders);
		}
	}

	private Map<String, String> getCookieMap(String cacheCookie) {
		Map<String, String> result = new HashMap<>();
		String[] cookieItems = cacheCookie.split(";");
		for (String item : cookieItems) {
			String[] keyVal = item.split("=");
			result.put(keyVal[0].trim(), keyVal[1].trim());
		}
		return result;
	}

}
