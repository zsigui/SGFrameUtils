package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.DownloadInfo;
import com.jackiezhuang.sgframework.utils.http.bean.DownloadRequest;
import com.jackiezhuang.sgframework.utils.http.itfc.IDownloadCallback;

/**
 * Created by JackieZhuang on 2015/9/5.
 */
public class DownloadController {

	private static final String TAG = DownloadController.class.getName();

	/**
	 * <p>下载状态指示：</p>
	 * READY：准备下载；DOWNLOADING：下载中；PAUSE：暂停下载；FINISHED：下载完成；DISCARD：取消下载或者文件被删除
	 */
	public enum DownloadStatus{
		READY, DOWNLOADING, PAUSE, FINISHED, DISCARD
	}

	private DownloadRequest mRequest;
	private DownloadDBUtil mDBUtil;
	private DownloadInfo mInfo;
	private IDownloadCallback mCallback;

	public DownloadController(DownloadDBUtil downloadDBUtil, DownloadInfo downloadInfo) {
		mDBUtil = downloadDBUtil;
		mInfo = downloadInfo;
	}

	public void pause() {
		if (mInfo.getStatus() == DownloadStatus.DOWNLOADING || mInfo.getStatus() == DownloadStatus.READY) {
			mInfo.setStatus(DownloadStatus.PAUSE.ordinal());
			// 写入当前完成到数据库
			mDBUtil.update(mInfo);
		}
	}

	/**
	 * 下载前的初始化工作
	 */
	public void init() {
		if (CommonUtil.isEmpty(mDBUtil.select(mInfo.getKey()))) {
			// 数据库不存在该记录，写入下载信息到数据库
			mInfo.setStatus(DownloadStatus.READY.ordinal());
			mDBUtil.insert(mInfo);
		}
	}

	public void resume() {
		if (mInfo.getStatus() == DownloadStatus.PAUSE) {
			// 读取此前中断内容
			mInfo.setStatus(DownloadStatus.READY.ordinal());
		}
		if (mInfo.getStatus() == DownloadStatus.DISCARD) {
			// 重新开始下载
		}
	}

	/**
	 * 指示下载
	 */
	public void start() {
		if (mInfo.getStatus() == DownloadStatus.READY) {
			mInfo.setStatus(DownloadStatus.DOWNLOADING.ordinal());
			// do http work
		}
	}

	public void finished() {
		if (mInfo.getStatus() == DownloadStatus.DOWNLOADING &&
				mInfo.getStopPos() == mInfo.getCurSize()) {
			// 更新数据库下载信息
			mInfo.setStatus(DownloadStatus.FINISHED.ordinal());
			mDBUtil.update(mInfo);
			mCallback.finished(mInfo);
		} else {
			// 文件出错，废弃下载
			L.i(TAG, "finished() : the file download meet with error!");
			mInfo.setStatus(DownloadStatus.DISCARD.ordinal());
			mInfo.setStopPos(0);
			mInfo.setStartPos(0);
			mInfo.setCurSize(0);
			mDBUtil.update(mInfo);
			mCallback.fail(mInfo);
		}
	}
}
