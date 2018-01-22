package com.track.cat.core.handler;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.track.cat.core.Definiens;
import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.handler.annotation.Handler;
import com.track.cat.core.handler.annotation.Service;
import com.track.cat.core.handler.exception.CatSystemException;
import com.track.cat.core.handler.interfaces.IInvoker;
import com.track.cat.core.handler.interfaces.IService;
import com.track.cat.util.FileUtil;

public class HandlerManager {
	private static Map<Class<?>, Object> context = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, IInvoker> workers = new ConcurrentHashMap<>();

	public static void init() {
		_scan(new File(FileUtil.getAppRoot() + File.separator + "src" + File.separator + "main" + File.separator
				+ "java" + File.separator + Definiens.SERVICE_PACKAGE.replaceAll("\\.", "/")), "");
	}

	private static void _scan(File root, String parent) {
		FileUtil.subFile(root).forEach(file -> {
			String name = file.getName();
			addWork(parent + "." + name);
		});

		FileUtil.subDir(root).forEach(file -> {
			String name = file.getName();
			_scan(file, parent + "." + name);
		});
	}

	private static void addWork(String name) {
		try {
			Class<?> clz = Class.forName(Definiens.SERVICE_PACKAGE + name.replace(".java", ""));
			if (!IService.class.isAssignableFrom(clz)) {
				return;
			}

			Service service = clz.getAnnotation(Service.class);
			if (service == null) {
				return;
			}

			if (!context.containsKey(clz)) {
				Constructor<?> constructor = clz.getConstructor();
				constructor.setAccessible(true);
				Object newInstance = constructor.newInstance();
				context.put(clz, newInstance);
			}

			Object newInstance = context.get(clz);

			String parentValue = service.value();

			Method[] methods = clz.getMethods();

			for (Method method : methods) {
				Handler handler = method.getAnnotation(Handler.class);
				if (handler != null) {
					String value = handler.value();
					workers.put(parentValue + value, FilterManager.link(invocation -> {
						try {
							return (Result) method.invoke(newInstance, invocation);
						} catch (Exception e) {
							throw new CatSystemException(e);
						}
					}));
				}
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new CatSystemException(e);
		}
	}

	public static Result handler(Invocation invocation) {
		String mapping = (String) invocation.getAttachment(Invocation.MAPPING);
		IInvoker invoker = workers.get(mapping);
		if (invoker == null) {
			throw new CatSystemException(mapping + " is not found");
		}
		return invoker.invoke(invocation);
	}

	public static ConcurrentMap<String, IInvoker> getWorkers() {
		return workers;
	}

}