package com.jackiezhuang.sgframework.utils.http.bean;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.chiper.MD5;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.io.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件上传请求类
 *
 * <p><p/>
 * Created by zsigui on 15-8-24.
 */
public class FileRequest extends HttpRequest {

	private static final String TAG = FileRequest.class.getName();

	private static final String DEFAULT_FILENAME = "DefaultName";
	/** 文本参数和字符集 */
	private static final String TYPE_TEXT= "text/plain";

	/** 字节流参数 */
	private static final String TYPE_OCTET_STREAM = "application/octet-stream";
	/**
	 * 文本格式
	 */
	private static final byte[] DIPOSITION_TEXT = "Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes();
	/**
	 * 二进制格式
	 */
	private static final byte[] DIPOSITION_BINARY = "Content-Transfer-Encoding: binary\r\n\r\n".getBytes();



	private String mBoundary = generateBoundary();
	private ByteArrayOutputStream mOutputData = new ByteArrayOutputStream();

	private String generateBoundary() {
		StringBuilder buider = new StringBuilder(40);
		buider.append("----");
		buider.append(MD5.digestInBase64(String.valueOf(System.currentTimeMillis()).getBytes()));
		buider.append("----");
		return buider.toString();
	}

	private void putParam(String key, String val) {
		if (CommonUtil.isEmpty(key) || CommonUtil.isEmpty(val)) {
			L.e(TAG, "putParam : key or val is not allowed to be null or \"\"");
			return;
		}
		writeToOutputData(key, val.getBytes(), TYPE_TEXT, DIPOSITION_TEXT, "");
	}

	private void putParam(String key, InputStream val) {
		if (CommonUtil.isEmpty(key) || CommonUtil.isEmpty(val)) {
			L.e(TAG, "putParam : key or val is not allowed to be null or \"\"");
			return;
		}
		writeToOutputData(key, FileUtil.readBytes(val), TYPE_OCTET_STREAM, DIPOSITION_BINARY, DEFAULT_FILENAME);
	}

	private void putParam(String key, File val) {
		if (CommonUtil.isEmpty(key) || CommonUtil.isEmpty(val)) {
			L.e(TAG, "putParam : key or val is not allowed to be null or \"\"");
			return;
		}
		writeToOutputData(key, FileUtil.readBytes(val), TYPE_OCTET_STREAM, DIPOSITION_BINARY, val.getName());
	}

	private void writeToOutputData(String name, byte[] rawData, String type, byte[] encodingType, String fileName) {
		try {
			mOutputData.write(String.format("--%s\r\n", mBoundary).getBytes());
			if (CommonUtil.isEmpty(fileName)) {
				mOutputData.write(String.format("Content-Disposition: form-data; name=\"%s\"\r\n", name).getBytes());
			} else {
				mOutputData.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n", name,
						fileName).getBytes());
			}
			mOutputData.write(String.format("Content-Type: %s\r\n\r\n", type).getBytes());
			mOutputData.write(encodingType);
			mOutputData.write(rawData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Priority getPriority() {
		return Priority.LOW;
	}

	@Override
	public String getContentType() {
		return "multipart/form-data; mBoundary = " + mBoundary;
	}

	@Override
	public byte[] getOutputData() {
		try {
			if (mOutputData.size() != 0) {
				mOutputData.write(String.format("--%s--\r\n", mBoundary).getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mOutputData.toByteArray();
	}
}
