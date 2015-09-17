package com.jackiezhuang.sgframework.utils.http.download;

import com.jackiezhuang.sgframework.utils.http.Dispatcher;
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
	@Override
	public void run() {
		mQuit = false;
		while (!mQuit) {
			try {
				DownloadInfo downloadInfo = mDownloadInfos.take();
				DownloadManager.INSTANCE.onDownloadStart(downloadInfo);
				if (judgeAndDealStatus(downloadInfo)) {
					if (initRange(downloadInfo)) {
						if (judgeAndDealStatus(downloadInfo)) {
							URL url = new URL(downloadInfo.getUrl());
							downloadInfo.setStartPos(downloadInfo.getCurSize());
							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							connection.setFixedLengthStreamingMode(downloadInfo.getStopPos() - downloadInfo.getStartPos() + 1);

							// 默认HttpURLConnection会进行Gzip压缩，这时无法通过getContentLength获取长度，所以要禁掉这个
							connection.setRequestProperty("Accept-Encoding", "identity");
							connection.setRequestMethod("GET");
							connection.setConnectTimeout(5000);
							connection.setReadTimeout(30000);
							if (downloadInfo.getStopPos() > 0) {
								connection.setRequestProperty("Range", "bytes=" + downloadInfo.getStartPos() + "-" + downloadInfo
										.getStopPos());
							} else {
								connection.setRequestProperty("Range", "bytes=" + downloadInfo.getStartPos() + "-");
							}
							if (connection.getResponseCode() == 200) {
								InputStream in = connection.getInputStream();
								BufferedInputStream bin = new BufferedInputStream(in);
								RandomAccessFile out = new RandomAccessFile(downloadInfo.getStorePath(), "rwd");
								out.seek(downloadInfo.getStartPos());
								int length = -2;
								byte[] bs = new byte[1024];
								while ((length = bin.read(bs)) != -1) {
									out.write(bs, 0, length);
									downloadInfo.setCurSize(downloadInfo.getCurSize() + length);
									DownloadManager.INSTANCE.onDownloadProgress(downloadInfo);
									if (downloadInfo.getStatus() != DownloadStatus.DOWNLOADING) {
										// 执行了暂停、取消、删除等操作
										break;
									}
								}
								if (downloadInfo.getStatus() == DownloadStatus.DOWNLOADING) {
									if (length == -1) {
										// 下载完成
										downloadInfo.setStatus(DownloadStatus.FINISHED.ordinal());
										mDBUtil.update(downloadInfo);
										DownloadManager.INSTANCE.onDownloadFinished(downloadInfo, DownloadError.SUCCESS);
									} else {
										// 此处状态错误，当废弃处理
										discardInfo(downloadInfo);
										DownloadManager.INSTANCE.onDownloadFinished(downloadInfo, DownloadError.ERR_STATE);
									}
								} else {
									judgeAndDealStatus(downloadInfo);
								}

							} else {
								// 连接错误，下载失败
								downloadInfo.setStatus(DownloadStatus.DISCARD.ordinal());
								DownloadManager.INSTANCE.onDownloadFinished(downloadInfo, DownloadError.ERR_CONNECT);
							}
						}
					} else {
						// 连接初始化下载信息失败
						downloadInfo.setStatus(DownloadStatus.DISCARD.ordinal());
						DownloadManager.INSTANCE.onDownloadFinished(downloadInfo, DownloadError.ERR_GET_SIZE);
					}
				}
				DownloadManager.INSTANCE.wakeToWork();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 判断Info是否处于下载状态，是返回true，否则根据状态对应处理
	 */
	private boolean judgeAndDealStatus(DownloadInfo downloadInfo) {
		boolean result = true;
		if (downloadInfo.getStatus() != DownloadStatus.DOWNLOADING) {
			result = false;
			switch (downloadInfo.getStatus()) {
				case READY:
					DownloadManager.INSTANCE.discardDownload(downloadInfo);
					DownloadManager.INSTANCE.startDownload(downloadInfo);
					break;
				case PAUSE:
					mDBUtil.update(downloadInfo);
					DownloadManager.INSTANCE.onDownloadFinished(downloadInfo, DownloadError.FAIL_PAUSE);
					break;
				case DISCARD:
					discardInfo(downloadInfo);
					DownloadManager.INSTANCE.onDownloadFinished(downloadInfo, DownloadError.FAIL_DISCARD);
					break;
				case REMOVED:
					// 删除下载，移除数据库记录
					FileUtil.delete(downloadInfo.getStorePath());
					mDBUtil.delete(downloadInfo.getKey());
					DownloadManager.INSTANCE.onDownloadFinished(downloadInfo, DownloadError.FAIL_REMOVED);
					break;
				case FINISHED:
					// 状态异常，当废弃处理
					discardInfo(downloadInfo);
					DownloadManager.INSTANCE.onDownloadFinished(downloadInfo, DownloadError.ERR_STATE);
			}
		}
		return result;
	}

	private void discardInfo(DownloadInfo downloadInfo) {
		downloadInfo.setStatus(DownloadStatus.DISCARD.ordinal());
		downloadInfo.setStopPos(0);
		downloadInfo.setStartPos(0);
		downloadInfo.setCurSize(0);
		FileUtil.delete(downloadInfo.getStorePath());
		mDBUtil.update(downloadInfo);
	}

	/**
	 * 初始化获取下载文件的长度
	 */
	private boolean initRange(DownloadInfo downloadInfo) throws IOException {
		if (downloadInfo.getStopPos() == 0) {
			URL url = new URL(downloadInfo.getUrl());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			// 默认HttpURLConnection会进行Gzip压缩，这时无法通过getContentLength获取长度，所以要禁掉这个
			connection.setRequestProperty("Accept-Encoding", "identity");
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
