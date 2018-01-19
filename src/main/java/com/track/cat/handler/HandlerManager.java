package com.track.cat.handler;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.track.cat.core.Definiens;
import com.track.cat.handler.annotation.Handler;
import com.track.cat.handler.annotation.Service;
import com.track.cat.handler.exception.MappingNotFoundExcption;
import com.track.cat.handler.interfaces.Invoker;
import com.track.cat.util.FileUtil;

public class HandlerManager {
	private static Map<Class<?>, Object> context = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, Invoker> workers = new ConcurrentHashMap<>();

	public static void init() {
		_scan(new File(FileUtil.getAppRoot() + File.separator + "src/main/java/"
				+ Definiens.SERVICE_PACKAGE.replaceAll("\\.", "/")));
	}

	private static void _scan(File root) {
		FileUtil.subFile(root).forEach(file -> {
			addWork(file);
		});

		FileUtil.subDir(root).forEach(file -> {
			_scan(file);
		});
	}

	private static void addWork(File file) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Class<?> clz = classLoader.loadClass(Definiens.SERVICE_PACKAGE + "."+file.getName().replace(".java", ""));
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
					workers.put(parentValue + value, FilterManager.link(new HandlerInvoker(method, newInstance)));
				}
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static Result handler(Invocation invocation) throws MappingNotFoundExcption {
		String mapping = (String) invocation.getAttachment(Invocation.MAPPING);
		Invoker invoker = workers.get(mapping);
		if (invoker == null) {
			throw new MappingNotFoundExcption(mapping);
		}
		return invoker.invoke(invocation);
	}

	public static ConcurrentMap<String, Invoker> getWorkers() {
		return workers;
	}

}
