package com.jackiezhuang.sgframework.utils.http.bean;

/**
 * Created by zsigui on 15-8-19.
 */
public abstract class UploadPart {

	private String name;
	private String val;
	private String contentType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}