package com.track.paint.core;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.track.paint.core.annotation.Handler;
import com.track.paint.core.annotation.Service;
import com.track.paint.core.exception.SystemException;
import com.track.paint.core.interfaces.IInvoker;
import com.track.paint.core.interfaces.IService;
import com.track.paint.util.ScanUtil;

public class HandlerManager {
	private static final Logger LOGGER = Logger.getLogger(HandlerManager.class);
	private static ConcurrentMap<String, BaseHandler> workers = new ConcurrentHashMap<>();

	public static void init() {
		LOGGER.info("scan the package to find handler in " + Definiens.SERVICE_PACKAGE);

		Set<Class<?>> classes = ScanUtil.getClasses(Definiens.SERVICE_PACKAGE);
		classes.forEach(clz -> {
			addWork(clz);
		});

		workers.forEach((k, v) -> {
			String methods = "";
			for (HttpMethod method : v.getMethods()) {
				methods += " " + method.name();
			}

			LOGGER.info("handler of " + k + " is loaded , method = {" + methods + " }");
		});
	}

	private static void addWork(Class<?> clz) {
		try {
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
							throw new SystemException(e);
						}
					});

					workers.put(parentValue + value, new BaseHandler(handler.method(), invoker));
				}
			}
		} catch (SecurityException | IllegalArgumentException e) {
			throw new SystemException(e);
		}
	}

	public static Result handler(Invocation invocation) {
		String mapping = (String) invocation.getAttachment(Invocation.MAPPING);
		IInvoker invoker = workers.get(mapping).getInvoker();
		if (invoker == null) {
			throw new SystemException(mapping + " is not found");
		}
		return invoker.invoke(invocation);
	}

	public static ConcurrentMap<String, BaseHandler> getWorkers() {
		return workers;
	}

}
