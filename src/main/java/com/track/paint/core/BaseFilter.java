package com.track.paint.core;

import com.track.paint.core.interfaces1.IFilter;

public class BaseFilter {
	private int index;
	private IFilter filter;

	public BaseFilter(int index, IFilter filter) {
		this.index = index;
		this.filter = filter;
	}

	public int getIndex() {
		return index;
	}

	public IFilter getFilter() {
		return filter;
	}

}