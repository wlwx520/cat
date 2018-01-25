package com.track.cat.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.track.cat.core.annotation.Filter;
import com.track.cat.core.exception.CatSystemException;
import com.track.cat.core.interfaces.IFilter;
import com.track.cat.core.interfaces.IInvoker;
import com.track.cat.util.FileUtil;

public class FilterManager {
	private static final Logger LOGGER = Logger.getLogger(FilterManager.class);
	private static List<IFilter> filters;

	private FilterManager() {
	}

	public static void init() {
		List<BaseFilter> inners = new ArrayList<>();
		String path = FileUtil.getAppRoot() + File.separator + "src" + File.separator + "main" + File.separator + "java"
				+ File.separator + Definiens.FILTER_PACKAGE.replaceAll("\\.", "/");

		LOGGER.info("scan the package to find filter in " + path);

		_scan(new File(path), "", inners);

		filters = inners.stream().sorted((a, b) -> {
			return a.getIndex() - b.getIndex();
		}).map(item -> {
			return item.getFilter();
		}).collect(Collectors.toList());

		filters.forEach(filter -> {
			LOGGER.info("filter of " + filter.getClass().getName() + " is loaded");
		});
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
			Class<?> clz = Class.forName(Definiens.FILTER_PACKAGE + name.replace(".java", ""));
			if (!IFilter.class.isAssignableFrom(clz)) {
				return;
			}

			Filter filter = clz.getAnnotation(Filter.class);
			if (filter == null) {
				return;
			}

			@SuppressWarnings("unchecked")
			IFilter newInstance = ApplicationContext.instance().getFilter((Class<IFilter>) clz);

			int index = filter.index();

			inners.add(new BaseFilter(index, newInstance));

		} catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
			throw new CatSystemException(e);
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
