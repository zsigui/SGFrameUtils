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
					return;
				}

				if (!response.isModified() && request.isDelivery()) {
					// 网络请求结果未变化且该请求已经分发过，无须再处理
					HttpManager.INSTANCE.finished(request);
					continue;
				}

				if (HttpConfig.sNeedCache && request.shouldCache() && response.isModified()) {
					// 添加或者更新Cache数据
					CacheManager.INSTANCE.putEntry(request.getRequestKey(), request.getCache());
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
		Map<String, String> additionHeaders = null;
		CacheHeader header = CacheManager.INSTANCE.getEntryHeader(request.getRequestKey());
		if (!CommonUtil.isEmpty(header)) {
			additionHeaders = new HashMap<>();
			if (!CommonUtil.isEmpty(header.getEtag())) {
				additionHeaders.put("If-None-Match", header.getEtag());
			}
			if (header.getServerTime() > 0) {
				additionHeaders.put("If-Modified-Since", DateUtil.formatGMTDate(DateUtil.getDate(header.getServerTime
						())));
			}
		}
		NetworkResponse response = mWorker.performRequest(request, additionHeaders);

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
			// 写入缓存
			CacheManager.INSTANCE.putEntry(request.getRequestKey(), HttpUtil.parseResponseHeader(response));
		}
		return result;
	}

}
