package com.track.cat.custom.filter;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.handler.annotation.Filter;
import com.track.cat.core.handler.interfaces.IFilter;
import com.track.cat.core.handler.interfaces.IInvoker;

@Filter(index = 1)
public class TestFilter1 implements IFilter {

	@Override
	public Result invoke(IInvoker invoker, Invocation invocation) {
		System.out.println("filter 1 before");
		Result invoke = invoker.invoke(invocation);
		System.out.println("filter 1 after");
		return invoke;
	}

}
