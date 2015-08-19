package com.jackiezhuang.sgframework.utils.http.bean;

/**
 * Created by zsigui on 15-8-19.
 */
public class MultiPart extends UploadPart {

	private String contentType;
	private String fileName;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
