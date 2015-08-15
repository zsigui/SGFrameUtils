package com.jackiezhuang.sgframework.utils;

import android.os.Build;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by zsigui on 15-8-11.
 */
public class HttpUtil {
	private static final String TAG = HttpUtil.class.toString();
	public static final String METHOD_POST = "POST";
	public static final String METHOD_GET = "GET";
	public static final int CONN_TIMEOUT = 5 * 1000;
	public static final int READ_TIMEOUT = 30 * 1000;

	public static void post(String requestUrl, Map<String, String> params) throws IOException {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD_MR1) {
			// 使用HttpClient 进行Post操作
		} else {
			// 使用Url
			URL url = new URL(requestUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod(METHOD_POST);
			urlConnection.setDoInput(true);
			urlConnection.setConnectTimeout(CONN_TIMEOUT);
			urlConnection.setReadTimeout(READ_TIMEOUT);
			urlConnection.setDefaultUseCaches(false);

			// 此部分设置传参数或上传文件
			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);   // 未知文件大小
			//urlConnection.setFixedLengthStreamingMode(10000); //已知文件大小

			urlConnection.connect();
			StringBuilder paramStr = new StringBuilder();
			if (params != null && params.size() > 0) {
				for (Map.Entry<String, String> param : params.entrySet()) {
					paramStr.append(URLEncoder.encode(param.getKey(), "UTF-8"));
					paramStr.append("=");
					paramStr.append(URLEncoder.encode(param.getValue(), "UTF-8"));
					paramStr.append("&");
				}
				paramStr.deleteCharAt(paramStr.length() - 1);
			}

			// 设置请求头参数
			urlConnection.setRequestProperty("Connection", "Keep-Alive");
			urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
			urlConnection.setRequestProperty("ContentType", "application/x-www-form-urlencoded; charset=UTF-8");
			urlConnection.setRequestProperty("Content-Length", String.valueOf(paramStr.length()));

			// post表单
			DataOutputStream bos = new DataOutputStream(urlConnection.getOutputStream());
			bos.write(paramStr.toString().getBytes());
			bos.flush();
			bos.close();


			if (urlConnection.getResponseCode() == 200) {
				byte[] buf = new byte[1024];
				String encoding = urlConnection.getContentEncoding();
				String contentType = urlConnection.getContentType();
				String charset = "UTF-8";
				try {
					charset = contentType.substring(contentType.indexOf("charset=") + 8);
				} catch (IndexOutOfBoundsException e) {

				}

				BufferedInputStream bis = null;
				if (encoding != null && encoding.contains("gzip")) {
					// 使用Gzip流方式进行读取
					bis = new BufferedInputStream(new GZIPInputStream(urlConnection.getInputStream()));
				} else {
					bis = new BufferedInputStream(urlConnection.getInputStream());
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream(bis.available());
				int length = 0;
				while ((length = bis.read(buf, 0, buf.length)) != -1) {
					baos.write(buf, 0, length);
				}
				baos.flush();
				baos.close();

				// 获取结果
				String result = baos.toString(charset);
			} else {
				L.i(TAG, "post return failed : code = " + urlConnection.getResponseCode() + ", " +
						"msg = " + urlConnection.getResponseMessage());
			}
		}
	}
}
