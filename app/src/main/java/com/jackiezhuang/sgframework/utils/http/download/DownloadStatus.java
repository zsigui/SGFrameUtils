package com.jackiezhuang.sgframework.utils.http.download;

/**
 * Created by zsigui on 15-9-14.
 */
public enum DownloadStatus {

	READY(0), DOWNLOADING(1), PAUSE(2), FINISHED(3), DISCARD(4), REMOVED(5);

	private int status = 0;

	private DownloadStatus(int status) {
		this.status = status;
	}

	public DownloadStatus get(int status) {
		if (status > 5 || status < 0) {
			throw new IllegalArgumentException("get(int) : status in bad range of integer");
		}
		return DownloadStatus.values()[status];
	}

	public int getCode() {
		return this.status;
	}
}
