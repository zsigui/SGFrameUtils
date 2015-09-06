package com.jackiezhuang.sgframework.utils.http;

/**
 * Created by JackieZhuang on 2015/9/5.
 */
public class DownloadController {

	/**
	 * <p>下载状态指示：</p>
	 * READY：准备下载；DOWNLOADING：下载中；PAUSE：暂停下载；FINISHED：下载完成；DISCARD：取消下载
	 */
	public enum DownloadStatus{
		READY, DOWNLOADING, PAUSE, FINISHED, DISCARD
	}

	public DownloadStatus mStatus;

	public void pause() {
		if (mStatus == DownloadStatus.DOWNLOADING || mStatus == DownloadStatus.READY) {
			mStatus = DownloadStatus.PAUSE;
			// 写入当前完成到数据库
		}
	}

	/**
	 * 下载前的初始化工作
	 */
	public void init() {
		// 写入下载信息到数据库
		// do something here

		mStatus = DownloadStatus.READY;
	}

	public void resume() {
		if (mStatus == DownloadStatus.PAUSE) {
			// 读取此前中断内容
			mStatus = DownloadStatus.READY;
		}
		if (mStatus == DownloadStatus.DISCARD) {
			// 重新开始下载
		}
	}

	/**
	 * 指示下载
	 */
	public void start() {
		if (mStatus == DownloadStatus.READY) {
			mStatus = DownloadStatus.DOWNLOADING;
			// do http work
		}
	}

	public void finished() {
		if (mStatus == DownloadStatus.DOWNLOADING) {
			// 移除数据库下载信息
			mStatus = DownloadStatus.FINISHED;
		}
	}
}
