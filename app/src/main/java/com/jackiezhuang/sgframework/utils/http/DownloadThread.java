package com.jackiezhuang.sgframework.utils.http;

import com.jackiezhuang.sgframework.utils.http.bean.DownloadInfo;
import com.jackiezhuang.sgframework.utils.http.impl.HttpConnectionWorker;
import com.jackiezhuang.sgframework.utils.http.itfc.IHttpWorker;
import com.jackiezhuang.sgframework.utils.io.FileUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by zsigui on 15-9-14.
 */
public class DownloadThread extends Dispatcher {

	// 惟一识别标志
	private int mTag;
	private PriorityBlockingQueue<DownloadInfo> mDownloadInfos;
	private DownloadDBUtil mDBUtil;
	private IHttpWorker mWorker = new HttpConnectionWorker();

	@Override
	public void run() {
		mQuit = false;
		while (!mQuit) {
			try {
				DownloadInfo downloadInfo = mDownloadInfos.take();
				if (downloadInfo.getStatus() == DownloadController.DownloadStatus.DOWNLOADING
						&& initRange(downloadInfo)) {
					URL url = new URL(downloadInfo.getUrl());
					downloadInfo.setStartPos(downloadInfo.getCurSize());
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setFixedLengthStreamingMode(downloadInfo.getStopPos() - downloadInfo.getStartPos() + 1);
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(30000);
					connection.setRequestProperty("Range", "bytes=" + downloadInfo.getStartPos() + "-" + downloadInfo
							.getStopPos());
					if (connection.getResponseCode() == 200) {
						InputStream in = connection.getInputStream();
						BufferedInputStream bin = new BufferedInputStream(in);
						RandomAccessFile out = new RandomAccessFile(downloadInfo.getStorePath(), "rwd");
						out.seek(downloadInfo.getStartPos());
						int length;
						byte[] bs = new byte[1024];
						while ((length = bin.read(bs)) != -1) {
							out.write(bs, 0, length);
							downloadInfo.setCurSize(downloadInfo.getCurSize() + length);
							DownloadManager.INSTANCE.onDownloadProgress(downloadInfo);
							if (downloadInfo.getCurSize() == downloadInfo.getStopPos() + 1) {
								downloadInfo.setStatus(DownloadController.DownloadStatus.FINISHED.ordinal());
								break;
							} else if (downloadInfo.getStatus() != DownloadController.DownloadStatus.DOWNLOADING) {
								// 执行了暂停、取消、删除等操作
								break;
							}
						}


						if (downloadInfo.getStatus() == DownloadController.DownloadStatus.FINISHED) {
							// 下载完成，回调成功下载
							mDBUtil.update(downloadInfo);
							DownloadManager.INSTANCE.onDownloadSuccess(downloadInfo);
						} else if (downloadInfo.getStatus() == DownloadController.DownloadStatus.DISCARD) {
							// 取消下载，删除文件
							downloadInfo.setStopPos(0);
							downloadInfo.setStartPos(0);
							downloadInfo.setCurSize(0);
							FileUtil.delete(downloadInfo.getStorePath());
							try {
								mDBUtil.update(downloadInfo);
								DownloadManager.INSTANCE.onDownloadFail(downloadInfo, "取消下载");
							} catch (Exception e) {
								// do nothing here
								// it will avoid the error of the null row in db
							}
						}

					} else {
						// 连接错误，下载失败
						downloadInfo.setStatus(DownloadController.DownloadStatus.DISCARD.ordinal());
						DownloadManager.INSTANCE.onDownloadFail(downloadInfo, "下载连接建立失败");
					}
				} else {
					// 连接初始化下载信息失败
					downloadInfo.setStatus(DownloadController.DownloadStatus.DISCARD.ordinal());
					DownloadManager.INSTANCE.onDownloadFail(downloadInfo, "获取下载文件大小失败");
				}
				DownloadManager.INSTANCE.wakeToWork();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean initRange(DownloadInfo downloadInfo) throws IOException {
		if (downloadInfo.getStopPos() == 0) {
			URL url = new URL(downloadInfo.getUrl());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			connection.connect();
			if (connection.getResponseCode() == 200) {
				downloadInfo.setStopPos(connection.getContentLength() - 1);
			}
			connection.disconnect();

			if (downloadInfo.getStopPos() != 0) {
				RandomAccessFile accessFile = new RandomAccessFile(downloadInfo.getStorePath(), "rwd");
				accessFile.setLength(downloadInfo.getStopPos() + 1);
				accessFile.close();
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
}
