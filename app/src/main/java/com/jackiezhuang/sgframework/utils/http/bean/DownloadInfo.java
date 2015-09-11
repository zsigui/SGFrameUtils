package com.jackiezhuang.sgframework.utils.http.bean;

import android.provider.BaseColumns;

import com.jackiezhuang.sgframework.utils.http.DownloadController;

/**
 * Created by zsigui on 15-9-10.
 */
public class DownloadInfo {

	private String mKey;
	private String mUrl;
	private String mStorePath;
	private int mStartPos;
	private int mStopPos;
	private int mCurSize;
	private DownloadController.DownloadStatus mStatus;

	public DownloadInfo() {
	}

	public String getKey() {
		return mKey;
	}

	public void setKey(String key) {
		mKey = key;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public String getStorePath() {
		return mStorePath;
	}

	public void setStorePath(String storePath) {
		mStorePath = storePath;
	}

	public int getStartPos() {
		return mStartPos;
	}

	public void setStartPos(int startPos) {
		mStartPos = startPos;
	}

	public int getStopPos() {
		return mStopPos;
	}

	public void setStopPos(int stopPos) {
		mStopPos = stopPos;
	}

	public int getCurSize() {
		return mCurSize;
	}

	public void setCurSize(int curSize) {
		mCurSize = curSize;
	}

	public DownloadController.DownloadStatus getStatus() {
		return mStatus;
	}

	public void setStatus(int status) {
		for (DownloadController.DownloadStatus downStatus : DownloadController.DownloadStatus.values()) {
			if (downStatus.ordinal() == status) {
				mStatus = downStatus;
				return;
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof DownloadInfo) && ((DownloadInfo) o).getKey().equals(this.mKey);
	}

	@Override
	public int hashCode() {
		return mKey.hashCode();
	}

	/**
	 * 定义数据库参数名
	 */
	public final class Param implements BaseColumns {

		public static final String TABLE_NAME = "download_info";

		public static final String _KEY = "_key";
		public static final String _URL = "_url";
		public static final String _STORE_PATH = "_store_path";
		public static final String _START_POS = "_start_pos";
		public static final String _STOP_POS = "_stop_pos";
		public static final String _CURRENT_SIZE = "_current_size";
		public static final String _STATE = "_state";
	}
}
