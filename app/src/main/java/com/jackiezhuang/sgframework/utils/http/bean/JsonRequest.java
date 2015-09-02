package com.jackiezhuang.sgframework.utils.http.bean;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;

/**
 * Json上传请求类
 *
 * <p></p>
 * Created by zsigui on 15-8-24.
 */
public class JsonRequest extends HttpRequest {

	private static final String TAG = JsonRequest.class.getName();

	private String mParam;


	public String getParam() {
		return mParam;
	}

	public void setParam(String param) {
		if (CommonUtil.isEmpty(param)) {
			L.e(TAG, "setParam : param is not allowed to be null or \"\"");
		}
		mParam = param;
	}

	@Override
	public Priority getPriority() {
		return Priority.NORMAL;
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public byte[] getOutputData() {
		if (CommonUtil.isEmpty(mParam)) {
			return new byte[0];
		}
		return mParam.getBytes();
	}
}
