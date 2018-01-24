package com.track.cat.core.handler;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.track.cat.core.ApplicationContext;
import com.track.cat.core.Definiens;
import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.annotation.Handler;
import com.track.cat.core.annotation.Service;
import com.track.cat.core.exception.CatSystemException;
import com.track.cat.core.interfaces.IInvoker;
import com.track.cat.core.interfaces.IService;
import com.track.cat.util.FileUtil;

public class HandlerManager {
	private static final Logger LOGGER = Logger.getLogger(HandlerManager.class);
	private static ConcurrentMap<String, BaseHandler> workers = new ConcurrentHashMap<>();

	public static void init() {
		String path = FileUtil.getAppRoot() + File.separator + "src" + File.separator + "main" + File.separator + "java"
				+ File.separator + Definiens.SERVICE_PACKAGE.replaceAll("\\.", "/");
		_scan(new File(path), "");
		LOGGER.info("scan the package to find handler in " + path);

		workers.forEach((k, v) -> {
			String methods = "";
			for (HttpMethod method : v.getMethods()) {
				methods += " " + method.name();
			}

			LOGGER.info("handler of " + k + " is loaded , method = {" + methods + " }");
		});
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

			@SuppressWarnings("unchecked")
			IService newInstance = ApplicationContext.instance().getService((Class<IService>) clz);

			String parentValue = service.value();

			Method[] methods = clz.getMethods();

			for (Method method : methods) {
				Handler handler = method.getAnnotation(Handler.class);
				if (handler != null) {
					String value = handler.value();
					IInvoker invoker = FilterManager.link(invocation -> {
						try {
							return (Result) method.invoke(newInstance, invocation);
						} catch (Exception e) {
							throw new CatSystemException(e);
						}
					});

					workers.put(parentValue + value, new BaseHandler(handler.method(), invoker));
				}
			}
		} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
			throw new CatSystemException(e);
		}
	}

	public static Result handler(Invocation invocation) {
		String mapping = (String) invocation.getAttachment(Invocation.MAPPING);
		IInvoker invoker = workers.get(mapping).getInvoker();
		if (invoker == null) {
			throw new CatSystemException(mapping + " is not found");
		}
		return invoker.invoke(invocation);
	}

	public static ConcurrentMap<String, BaseHandler> getWorkers() {
		return workers;
	}

}
