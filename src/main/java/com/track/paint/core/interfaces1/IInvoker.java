package com.track.paint.core.interfaces1;

import com.track.paint.core.Invocation;
import com.track.paint.core.Result;

@FunctionalInterface
public interface IInvoker {
	public Result invoke(Invocation invocation);
}
