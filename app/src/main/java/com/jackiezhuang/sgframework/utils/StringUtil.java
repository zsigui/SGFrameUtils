package com.jackiezhuang.sgframework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zsigui on 15-8-21.
 */
public class StringUtil {

	private static Matcher getMatcher(CharSequence data, String pattern, int flag) {
		Pattern p = Pattern.compile(pattern, flag);
		return p.matcher(data);
	}

	public static boolean find(CharSequence data, String pattern, boolean isSensitive) {
		return getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE).find();
	}

	public static boolean matches(CharSequence data, String pattern, boolean isSensitive) {
		return getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE).matches();
	}

	public static String findMatch(CharSequence data, String pattern, int groupIndex, boolean isSensitive) {
		Matcher matcher = getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		String result = null;
		if (matcher.find()) {
			result = matcher.group(groupIndex);
		}
		return result;
	}

	public static String findMatch(CharSequence data, String pattern, int groupIndex, String defaultNotFound,
	                               boolean isSensitive) {
		Matcher matcher = getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		String result;
		if (matcher.find()) {
			result = matcher.group(groupIndex);
		} else {
			result = defaultNotFound;
		}
		return result;
	}

	public static List<String> findAllMatch(CharSequence data, String pattern, int groupIndex, boolean isSensitive) {
		List<String> result = new ArrayList<>();
		Matcher matcher = getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		while (matcher.find()) {
			result.add(matcher.group(groupIndex));
		}
		return result;
	}
}
