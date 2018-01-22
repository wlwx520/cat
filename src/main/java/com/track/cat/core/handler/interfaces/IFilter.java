package com.track.cat.core.handler.interfaces;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;

public interface IFilter {
	Result invoke(IInvoker invoker, Invocation invocation);
}