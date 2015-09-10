package com.jackiezhuang.sgframework.utils.http;

import android.content.Context;

import com.jackiezhuang.sgframework.utils.L;
import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.http.bean.DownloadInfo;
import com.jackiezhuang.sgframework.utils.http.bean.DownloadRequest;

import java.util.Map;

/**
 * Created by JackieZhuang on 2015/9/10.
 */
public enum  DownloadManager {

	INSTANCE;

	private static final String TAG = DownloadManager.class.getName();
	private DownloadDBUtil mDBUtil;
	private boolean mIsInit;
	private Map<String, DownloadInfo> mInfoMap;

	public boolean init(Context context) {
		if (!CommonUtil.isEmpty(context)) {
			L.e(TAG, "init(Context) : param is not allowed to be null");
			mIsInit = false;
			return mIsInit;
		}
		mDBUtil = new DownloadDBUtil(context);
		mInfoMap = mDBUtil.selectAll();
		mIsInit = true;
		return mIsInit;
	}

	public void start() {
		if (!mIsInit) {
			throw new RuntimeException("start() : you need to call init(Context) first");
		}
	}

	public void add(DownloadRequest request) {

	}

}
