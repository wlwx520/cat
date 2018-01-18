package com.track.cat.handler;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.track.cat.core.Definiens;
import com.track.cat.handler.interfaces.Filter;
import com.track.cat.handler.interfaces.Invoker;
import com.track.cat.util.FileUtil;

public class FilterManager {
	private static FilterManager instance = new FilterManager();
	private static Map<Class<?>, Object> context = new ConcurrentHashMap<>();
	private List<Filter> filters = new ArrayList<>();

	public static FilterManager instance() {
		return instance;
	}

	public void init() {
		_scan(new File(FileUtil.getAppRoot() + File.separator + Definiens.SERVICE_PACKAGE));
	}

	private void _scan(File root) {
		FileUtil.subFile(root).forEach(file -> {
			addFilter(file);
		});

		FileUtil.subDir(root).forEach(file -> {
			_scan(file);
		});
	}

	private void addFilter(File file) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Class<?> clz = classLoader.loadClass(Definiens.SERVICE_PACKAGE + file.getName().replace(".java", ""));
			if (!clz.isAssignableFrom(Filter.class)) {
				return;
			}

			if (!context.containsKey(clz)) {
				Constructor<?> constructor = clz.getConstructor();
				constructor.setAccessible(true);
				Object newInstance = constructor.newInstance();
				context.put(clz, newInstance);
			}

			Filter newInstance = (Filter) context.get(clz);
			filters.add(newInstance);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public Invoker link(final Invoker invoker) {
		Invoker last = invoker;
		if (!filters.isEmpty()) {
			for (int i = filters.size() - 1; i >= 0; i--) {
				Filter filter = filters.get(i);
				Invoker next = last;
				last = new Invoker() {
					public Result invoke(Invocation invocation) {
						return filter.invoke(next, invocation);
					}
				};
			}
		}
		return last;
	}

}
