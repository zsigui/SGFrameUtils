package com.jackiezhuang.sgframework.utils.http;

import android.content.Context;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.DownloadInfo;
import com.jackiezhuang.sgframework.utils.http.bean.DownloadRequest;
import com.jackiezhuang.sgframework.utils.http.itfc.IDownloadCallback;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by JackieZhuang on 2015/9/10.
 */
public enum  DownloadManager {

	INSTANCE;

	private static final String TAG = DownloadManager.class.getName();
	private static final int mMaxThreadCount = 3;
	private int mCurThreadCount;
	private DownloadDBUtil mDBUtil;
	private boolean mIsInit;
	private IDownloadCallback mCallback;
	private PriorityBlockingQueue<DownloadInfo> mDownloadQueue;
	private List<DownloadInfo> mReadyQueue;
	private Thread[] mNetWorker;

	public IDownloadCallback getCallback() {
		return mCallback;
	}

	/**
	 * 通过实现此回调接口来对下载管理器行为结果进行处理
	 */
	public void setCallback(IDownloadCallback callback) {
		mCallback = callback;
	}

	public boolean init(Context context) {
		if (!CommonUtil.isEmpty(context)) {
			L.e(TAG, "init(Context) : param is not allowed to be null");
			mIsInit = false;
			return mIsInit;
		}
		mDBUtil = new DownloadDBUtil(context);
		mIsInit = true;
		if (mCallback != null) {
			// 回调所有的事件
			mCallback.init(mDBUtil.selectAll());
		}
		return mIsInit;
	}

	public void start() {
		if (!mIsInit) {
			throw new RuntimeException("start() : you need to call init(Context) first");
		}
		mNetWorker = new Thread[mMaxThreadCount];
		mDownloadQueue = new PriorityBlockingQueue<>(mMaxThreadCount);
		mDownloadQueue.addAll(mDBUtil.select(DownloadController.DownloadStatus.DOWNLOADING.ordinal()));
		mReadyQueue = mDBUtil.select(DownloadController.DownloadStatus.READY.ordinal());
		mCurThreadCount = 0;
		wakeToWork();
	}


	/**
	 * 当线程有空闲，添加新的到正在运行中
	 */
	public synchronized void wakeToWork() {
		mDownloadQueue.add(mReadyQueue.remove(0));
	}

	private void discard(String key) {
		DownloadInfo info;
		if ((info = obtainByKey(key)) != null) {
			L.e(TAG, "stop(String) : no task with given key to be discarded");
			return;
		}
		if (info.getStatus() == DownloadController.DownloadStatus.DOWNLOADING) {

		}
		mDBUtil.delete(key);
	}

	private DownloadInfo obtainByKey(String key) {

		return null;
	}

	public void add(DownloadRequest request) {

	}

	public void remove(DownloadInfo info) {
		mDBUtil.delete(info.getKey());
		if (mCallback != null) {
			mCallback.remove(info);
		}
	}

	public void onDownloadProgress(DownloadInfo info) {

	}

	public void onDownloadStart(DownloadInfo info){

	}

	public void onDownloadFail(DownloadInfo downloadInfo, String errMsg) {
	}

	public void onDownloadSuccess(DownloadInfo downloadInfo) {
	}
}
