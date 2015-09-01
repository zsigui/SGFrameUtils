package com.jackiezhuang.sgframework.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by zsigui on 15-9-1.
 */
public class DateUtil {

	public static final String PATTERN_GMT = "EEEE, dd MMM yyyy HH:mm:ss zzz";

	public static String formatGMTDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_GMT, Locale.getDefault());
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}

	public static Date parseGMTDate(String date) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_GMT, Locale.getDefault());
			return dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getVal(String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
		return sdf.format(getTime());
	}

	/**
	 * 获取起始到现在的时间戳(ms)
	 * @return
	 */
	public static long getTimeInMillis() {
		return Calendar.getInstance(Locale.getDefault()).getTimeInMillis();
	}

	/**
	 * 获取Unix时间戳(ms)
	 * @return
	 */
	public static long getGMTUnixTimeInMillis() {
		return getTimeInMillis() - TimeZone.getDefault().getRawOffset();
	}

	/**
	 * 获取起始到现在的日期
	 * @return
	 */
	public static Date getTime() {
		return Calendar.getInstance(Locale.getDefault()).getTime();
	}

	public static Date getDate(long serverTime) {
		return new Date(serverTime);
	}
}
