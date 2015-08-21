package com.jackiezhuang.sgframework.utils.http.bean;

/**
 * Created by zsigui on 15-8-19.
 */
public class MultiPart extends UploadPart {


	private String contentEndcoding;
	private String fileName;


	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentEndcoding() {
		return contentEndcoding;
	}

	public void setContentEndcoding(String contentEndcoding) {
		this.contentEndcoding = contentEndcoding;
	}
}
