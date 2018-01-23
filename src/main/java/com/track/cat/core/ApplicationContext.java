package com.track.cat.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.track.cat.core.annotation.AutoLifeCycle;
import com.track.cat.core.exception.CatSystemException;

public class ApplicationContext {
	private static final Logger LOGGER = Logger.getLogger(ApplicationContext.class);
	public final Map<Class<?>, Object> BEANS = new ConcurrentHashMap<>();

	private static final ApplicationContext instance = new ApplicationContext();

	private ApplicationContext() {
	}

	public static ApplicationContext instance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> clz) {
		LOGGER.debug("get bean of " + clz.getName());
		if (BEANS.containsKey(clz)) {
			return (T) BEANS.get(clz);
		}
		LOGGER.debug("bean of " + clz.getName() + " is not found ,prepare to create");

		try {
			Constructor<T> constructor = clz.getConstructor();
			constructor.setAccessible(true);
			T newInstance = constructor.newInstance();

			Field[] fields = clz.getDeclaredFields();
			for (Field field : fields) {
				if (field.getAnnotation(AutoLifeCycle.class) != null) {
					field.setAccessible(true);
					field.set(newInstance, getBean(field.getType()));
				}
			}
			LOGGER.debug("bean of " + clz.getName() + " created");
			BEANS.put(clz, newInstance);
			return newInstance;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new CatSystemException(e);
		}
	}
}
