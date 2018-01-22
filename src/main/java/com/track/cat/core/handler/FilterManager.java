package com.track.cat.core.handler;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.track.cat.core.Definiens;
import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.handler.annotation.Filter;
import com.track.cat.core.handler.interfaces.IFilter;
import com.track.cat.core.handler.interfaces.IInvoker;
import com.track.cat.util.FileUtil;

public class FilterManager {
	private static Map<Class<?>, Object> context = new ConcurrentHashMap<>();
	private static List<IFilter> filters;

	private FilterManager() {
	}

	public static void init() {
		List<BaseFilter> inners = new ArrayList<>();
		_scan(new File(FileUtil.getAppRoot() + File.separator + "src" + File.separator + "main" + File.separator
				+ "java" + File.separator + Definiens.SERVICE_PACKAGE.replaceAll("\\.", "/")), "", inners);

		filters = inners.stream().sorted((a, b) -> {
			return a.getIndex() - b.getIndex();
		}).map(item -> {
			return item.getFilter();
		}).collect(Collectors.toList());
	}

	private static void _scan(File root, String parent, List<BaseFilter> inners) {
		FileUtil.subFile(root).forEach(file -> {
			String name = file.getName();
			addFilter(parent + "." + name, inners);
		});

		FileUtil.subDir(root).forEach(file -> {
			String name = file.getName();
			_scan(file, parent + "." + name, inners);
		});
	}

	private static void addFilter(String name, List<BaseFilter> inners) {
		try {
			Class<?> clz = Class.forName(Definiens.SERVICE_PACKAGE + name.replace(".java", ""));
			if (!IFilter.class.isAssignableFrom(clz)) {
				return;
			}

			Filter filter = clz.getAnnotation(Filter.class);
			if (filter == null) {
				return;
			}

			if (!context.containsKey(clz)) {
				Constructor<?> constructor = clz.getConstructor();
				constructor.setAccessible(true);
				Object newInstance = constructor.newInstance();
				context.put(clz, newInstance);
			}

			IFilter newInstance = (IFilter) context.get(clz);

			int index = filter.index();

			inners.add(new BaseFilter(index, newInstance));
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static IInvoker link(final IInvoker invoker) {
		IInvoker last = invoker;
		if (!filters.isEmpty()) {
			for (int i = filters.size() - 1; i >= 0; i--) {
				IFilter filter = filters.get(i);
				IInvoker next = last;
				last = new IInvoker() {
					public Result invoke(Invocation invocation) {
						return filter.invoke(next, invocation);
					}
				};
			}
		}
		return last;
	}

}
