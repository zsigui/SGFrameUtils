package com.jackiezhuang.sgframework.utils.http.bean;

import com.jackiezhuang.sgframework.utils.chiper.MD5;

import java.io.File;

/**
 * 下载请求类
 *
 * <p></p>
 * Created by zsigui on 15-9-2.
 */
public class DownloadRequest extends HttpRequest{

	private File mStoreFile;
	private String mTempPath;

	@Override
	public Priority getPriority() {
		return Priority.LOW;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public byte[] getOutputData() {
		return new byte[0];
	}

	public void handleRespContent(NetworkResponse response) {

	}

	@Override
	public String getRequestKey() {
		return MD5.digestInHex((super.getRequestKey() + mStoreFile.getName()).getBytes());
	}
}
