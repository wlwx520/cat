package com.track.cat.core.interfaces;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;

@FunctionalInterface
public interface IInvoker {
	public Result invoke(Invocation invocation);
}
