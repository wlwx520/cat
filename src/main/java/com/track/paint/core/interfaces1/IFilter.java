package com.track.paint.core.interfaces1;

import com.track.paint.core.Invocation;
import com.track.paint.core.Result;

public interface IFilter {
	Result invoke(IInvoker invoker, Invocation invocation);
}