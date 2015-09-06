package com.jackiezhuang.sgframework.utils.http.bean;

/**
 * 下载请求类
 *
 * <p></p>
 * Created by zsigui on 15-9-2.
 */
public class DownloadRequest extends HttpRequest{

	@Override
	public Priority getPriority() {
		return null;
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
}
