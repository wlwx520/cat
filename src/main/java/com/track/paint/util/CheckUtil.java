package com.track.paint.util;

import java.lang.reflect.Field;

import com.track.paint.core.exception.SystemException;

public class CheckUtil {
	public static void checkPrimaryKey(Field field) {
		if (!(field.getType().isAssignableFrom(int.class) || field.getType().isAssignableFrom(Integer.class)
				|| field.getType().isAssignableFrom(long.class) || field.getType().isAssignableFrom(Long.class))) {
			throw new SystemException("primary key be long or integer");
		}
	}

	public static boolean checkText(Class<?> type) {
		return type.isAssignableFrom(String.class) || type.isAssignableFrom(boolean.class)
				|| type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(char.class)
				|| type.isAssignableFrom(Character.class);
	}

	public static boolean checkDecimal(Class<?> type) {
		return type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)
				|| type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class);
	}

	public static boolean checkNumber(Class<?> type) {
		return type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)
				|| type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)
				|| type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class);
	}

}
