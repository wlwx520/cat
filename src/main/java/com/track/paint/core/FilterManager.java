package com.track.paint.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.track.paint.core.annotation.Filter;
import com.track.paint.core.exception.SystemException;
import com.track.paint.core.filter.LogDebugFilter;
import com.track.paint.core.interfaces.IFilter;
import com.track.paint.core.interfaces.IInvoker;
import com.track.paint.util.ScanUtil;

public class FilterManager {
	private static final Logger LOGGER = Logger.getLogger(FilterManager.class);
	private static List<IFilter> filters;

	private FilterManager() {
	}

	public static void init() {
		List<BaseFilter> inners = new ArrayList<>();

		IFilter newInstance = ApplicationContext.instance().getFilter(LogDebugFilter.class);

		inners.add(new BaseFilter(0, newInstance));

		LOGGER.info("scan the package to find filter in " + Definiens.FILTER_PACKAGE);

		Set<Class<?>> classes = ScanUtil.getClasses(Definiens.FILTER_PACKAGE);
		classes.forEach(clz -> {
			addFilter(clz, inners);
		});

		filters = inners.stream().sorted((a, b) -> {
			return a.getIndex() - b.getIndex();
		}).map(item -> {
			return item.getFilter();
		}).collect(Collectors.toList());

		filters.forEach(filter -> {
			LOGGER.info("filter of " + filter.getClass().getName() + " is loaded");
		});
	}

	private static void addFilter(Class<?> clz, List<BaseFilter> inners) {
		try {
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

		} catch (SecurityException | IllegalArgumentException e) {
			throw new SystemException(e);
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
