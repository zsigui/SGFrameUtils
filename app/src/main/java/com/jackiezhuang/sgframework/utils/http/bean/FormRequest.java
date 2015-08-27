package com.jackiezhuang.sgframework.utils.http.bean;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.SGConfig;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 表单请求类
 *
 * <p></p>
 * Created by zsigui on 15-8-24.
 */
public class FormRequest extends HttpRequest {

	private static final String TAG = FormRequest.class.getName();

	// 表单参数
	private Map<String, String> params = null;

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		if (CommonUtil.isEmpty(params)) {
			L.e(TAG, "setParams : params is not allowed to be null");
			//throw new IllegalArgumentException(TAG + ".setParams : params is not allowed to be null");
		}
		this.params = params;
	}

	public void putParam(String key, String val) {
		if (CommonUtil.isEmpty(params)) {
			params = new HashMap<>();
		}
		if (CommonUtil.isEmpty(key) || CommonUtil.isEmpty(val)) {
			L.e(TAG, "putParam : key or val is not allowed to be null or \"\"");
			//throw new IllegalArgumentException(TAG + ".putParam : key or val is not allowed to be null or \"\"");
			return;
		}
		params.put(key, val);
	}

	public String getParam(String key) {
		return params.get(key);
	}

	public String toUrlParams() {
		return toUrlParams(SGConfig.DEFAULT_UTF_CHARSET);
	}

	public String toUrlParams(String encodeCharset) {
		if (CommonUtil.isEmpty(params)) {
			L.e(TAG, "toUrlParams : you need to call method putParam or setParams first");
			return null;
		}
		final StringBuilder result = new StringBuilder();
		try {
			if (params != null && params.size() > 0) {
				for (Map.Entry<String, String> param : params.entrySet()) {
					result.append(URLEncoder.encode(param.getKey(), encodeCharset));
					result.append("=");
					result.append(URLEncoder.encode(param.getValue(), encodeCharset));
					result.append("&");
				}
				result.deleteCharAt(result.length() - 1);
			}
		} catch (Exception e) {
			L.e(TAG, String.format("toUrlParams : the charset %s is not supported", encodeCharset));
			e.printStackTrace();
		}
		return result.toString();
	}

	@Override
	public Priority getPriority() {
		return Priority.NORMAL;
	}

	@Override
	public String getContentType() {
		return "application/x-www-form-urlencoded; charset=UTF-8";
	}

	@Override
	public byte[] getOutputData() {
		String result = toUrlParams();
		if (CommonUtil.isEmpty(result)) {
			return new byte[0];
		}
		return result.getBytes();
	}
}
