package com.jackiezhuang.sgframework.utils.http.itfc;

import com.jackiezhuang.sgframework.utils.http.bean.DownloadInfo;

import java.util.List;

/**
 * Created by zsigui on 15-9-11.
 */
public interface IDownloadCallback {

	void init(List<DownloadInfo> downloadInfos);

	void start(DownloadInfo downloadInfo);

	void download(DownloadInfo downloadInfo);

	void stop(DownloadInfo downloadInfo);

	void remove(DownloadInfo key);

	void finished(DownloadInfo info);

	void fail(DownloadInfo info);
}
