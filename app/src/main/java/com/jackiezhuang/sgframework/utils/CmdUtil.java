package com.jackiezhuang.sgframework.utils;

import com.jackiezhuang.sgframework.utils.common.CommonUtil;
import com.jackiezhuang.sgframework.utils.io.IOUtil;

import java.io.IOException;

/**
 * Created by zsigui on 15-9-1.
 */
public class CmdUtil {

	private static final String TAG = CmdUtil.class.toString();

	public static byte[] exec(String cmd) {
		byte[] result = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec(cmd);
			if (proc.waitFor() == 0) {
				result = IOUtil.readBytes(proc.getInputStream());
			} else {
				result = IOUtil.readBytes(proc.getErrorStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
			L.e(TAG, "execCmdToList(String) : 执行命令出错：" + cmd);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String execCmd(String cmd) {
		return CommonUtil.bytesToStr(exec(cmd));
	}
}
