package com.jackiezhuang.sgframework.utils.http.download;

import android.content.Context;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.itfc.IDownloadCallback;
import com.jackiezhuang.sgframework.utils.io.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by JackieZhuang on 2015/9/10.
 */
public enum  DownloadManager {

	INSTANCE;

	private static final String TAG = DownloadManager.class.getName();
	private static final int mMaxThreadCount = 3;
	private DownloadDBUtil mDBUtil;
	private boolean mIsInit;
	private IDownloadCallback mCallback;
	private List<DownloadInfo> mAllDownloadQueue;
	private PriorityBlockingQueue<DownloadInfo> mDownloadQueue;
	private List<DownloadInfo> mReadyQueue;
	private DownloadThread[] mNetWorker;

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
		mAllDownloadQueue = mDBUtil.selectAll();
		if (mCallback != null) {
			// 回调，返回所有保存的下载信息
			mCallback.init(mAllDownloadQueue);
		}
		mDownloadQueue = new PriorityBlockingQueue<>(mMaxThreadCount);
		mReadyQueue = new ArrayList<>();
		for (DownloadInfo downloadInfo : mAllDownloadQueue) {
			if (downloadInfo.getStatus() == DownloadStatus.DOWNLOADING) {
				mDownloadQueue.add(downloadInfo);
			} else if (downloadInfo.getStatus() == DownloadStatus.READY) {
				mReadyQueue.add(downloadInfo);
			}
		}
		return mIsInit;
	}

	public void start() {
		if (!mIsInit) {
			throw new RuntimeException("start() : you need to call init(Context) first");
		}
		stop();
		mNetWorker = new DownloadThread[mMaxThreadCount];
		for (int i = 0; i< mNetWorker.length; i++) {
			mNetWorker[i] = new DownloadThread();
			mNetWorker[i].start();
		}
	}

	private void stop() {
		if (CommonUtil.isEmpty(mNetWorker)) {
			return;
		}
		for (int i = 0; i< mNetWorker.length; i++) {
			if (!CommonUtil.isEmpty(mNetWorker[i])) {
				mNetWorker[i].exit();
				mNetWorker[i] = null;
			}
		}
		mNetWorker = null;
	}


	private DownloadInfo findInfoInAllQueue(String urlOrKey) {
		DownloadInfo result = null;
		for (DownloadInfo info :  mAllDownloadQueue) {
			if (urlOrKey.equals(info.getUrl()) || urlOrKey.equals(info.getKey())) {
				result = info;
			}
		}
		return result;
	}

	private void pauseDownload(String urlOrKey) {
		DownloadInfo result = findInfoInAllQueue(urlOrKey);
		if (result != null) {
			if (result.getStatus() == DownloadStatus.READY) {
				result.setStatus(DownloadStatus.PAUSE.ordinal());
				mReadyQueue.remove(result);
				mDBUtil.update(result);
			} else if (result.getStatus() == DownloadStatus.DOWNLOADING) {
				result.setStatus(DownloadStatus.PAUSE.ordinal());
			}
		}
	}

	public void pauseDownload(DownloadInfo info) {
		pauseDownload(info.getKey());
	}

	public void startDownload(String urlOrKey) {
		DownloadInfo result = findInfoInAllQueue(urlOrKey);
		if (result != null) {
			result.setStatus(DownloadStatus.READY.ordinal());
			mReadyQueue.add(result);
			mDBUtil.update(result);
			wakeToWork();
		}
	}

	public void startDownload(DownloadInfo info) {
		startDownload(info.getKey());
	}

	public void discardDownload(String urlOrKey) {
		DownloadInfo info = findInfoInAllQueue(urlOrKey);
		if (info.getStatus() != DownloadStatus.DOWNLOADING) {
			info.setStatus(DownloadStatus.DISCARD.ordinal());
		} else {
			if (info.getStatus() == DownloadStatus.READY ) {
				// 处于待执行状态
				mReadyQueue.remove(info);
			}
			discardInfo(info);
			DownloadManager.INSTANCE.onDownloadFinished(info, DownloadError.FAIL_DISCARD);
		}

	}

	public void discardDownload(DownloadInfo downloadInfo) {
		discardDownload(downloadInfo.getKey());
	}

	public void removeDownload(String urlOrKey) {
		DownloadInfo info = findInfoInAllQueue(urlOrKey);
		if (info.getStatus() != DownloadStatus.DOWNLOADING) {
			info.setStatus(DownloadStatus.REMOVED.ordinal());
		} else {
			if (info.getStatus() == DownloadStatus.READY ) {
				// 处于待执行状态
				mReadyQueue.remove(info);
			}
			FileUtil.delete(info.getStorePath());
			mDBUtil.delete(info.getKey());
			DownloadManager.INSTANCE.onDownloadFinished(info, DownloadError.FAIL_REMOVED);
		}
		mAllDownloadQueue.remove(info);
	}

	public void removeDownload(DownloadInfo info) {
		removeDownload(info.getKey());
	}

	/**
	 * 当线程有空闲，添加新的等待下载任务到正在运行中
	 */
	public synchronized void wakeToWork() {
		if (mDownloadQueue.size() < mMaxThreadCount) {
			DownloadInfo info = mReadyQueue.remove(0);
			info.setStatus(DownloadStatus.DOWNLOADING.ordinal());
			mDownloadQueue.add(info);
		}
	}

	private void discardInfo(DownloadInfo downloadInfo) {
		downloadInfo.setStatus(DownloadStatus.DISCARD.ordinal());
		downloadInfo.setStopPos(0);
		downloadInfo.setStartPos(0);
		downloadInfo.setCurSize(0);
		FileUtil.delete(downloadInfo.getStorePath());
		mDBUtil.update(downloadInfo);
	}

	public void addDownload(DownloadInfo info) {
		if (mAllDownloadQueue.contains(info)) {
			L.e(TAG, "add(DownloadInfo) : this download is in queue");
			return;
		}
		if (CommonUtil.isEmpty(info.getUrl())) {
			L.e(TAG, "add(DownloadInfo) : the download url is unknown");
			return;
		}
		if (CommonUtil.isEmpty(info.getStorePath())) {
			L.e(TAG, "add(DownloadInfo) : no store path to be defined");
			return;
		}
		info.setStatus(DownloadStatus.READY.ordinal());
		mAllDownloadQueue.add(info);
		mReadyQueue.add(info);
		mDBUtil.update(info);
	}

	public void restartDownload(DownloadInfo info) {
		DownloadInfo result = findInfoInAllQueue(info.getKey());
		if (result != null) {
			if (result.getStatus() == DownloadStatus.READY) {
				result.setStopPos(0);
				result.setStartPos(0);
				result.setCurSize(0);
				result.setStatus(DownloadStatus.READY.ordinal());
				FileUtil.delete(result.getStorePath());
				mReadyQueue.remove(info);
				startDownload(info);
			} else if (result.getStatus() == DownloadStatus.DOWNLOADING) {
				result.setStatus(DownloadStatus.READY.ordinal());
			} else {
				discardInfo(info);
				startDownload(info);
			}
		}
	}

	public void addOrRestartDownload(DownloadInfo info) {
		if (mAllDownloadQueue.contains(info)) {
			restartDownload(info);
		} else {
			addDownload(info);
		}
	}

	public void onDownloadProgress(DownloadInfo info) {
		if (mCallback != null) {
			mCallback.download(info);
		}
	}

	public void onDownloadStart(DownloadInfo info){
		if (mCallback != null) {
			mCallback.start(info);
		}
	}

	public void onDownloadFinished(DownloadInfo info, int errCode) {
		if (mCallback != null) {
			mCallback.finished(info, errCode);
		}
	}
}
