package com.jackiezhuang.sgframework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zsigui on 15-8-21.
 */
public class StringUtil {

	private static Matcher getMatcher(CharSequence data, String pattern) {
		Pattern p = Pattern.compile(pattern);
		return p.matcher(data);
	}

	public static boolean find(CharSequence data, String pattern) {
		return getMatcher(data, pattern).find();
	}

	public static boolean matches(CharSequence data, String pattern) {
		return getMatcher(data, pattern).matches();
	}

	public static String findMatch(CharSequence data, String pattern, int groupIndex) {
		Matcher matcher = getMatcher(data, pattern);
		String result = null;
		if (matcher.find()) {
			result = matcher.group(groupIndex);
		}
		return result;
	}

	public static String findMatch(CharSequence data, String pattern, int groupIndex, String defaultNotFound) {
		Matcher matcher = getMatcher(data, pattern);
		String result = null;
		if (matcher.find()) {
			result = matcher.group(groupIndex);
		} else {
			result = defaultNotFound;
		}
		return result;
	}

	public static List<String> findAllMatch(CharSequence data, String pattern, int groupIndex) {
		List<String> result = new ArrayList<>();
		Matcher matcher = getMatcher(data, pattern);
		while (matcher.find()) {
			result.add(matcher.group(groupIndex));
		}
		return result;
	}
}
