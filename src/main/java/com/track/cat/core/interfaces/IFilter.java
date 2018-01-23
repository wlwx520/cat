package com.track.cat.core.interfaces;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;

public interface IFilter {
	Result invoke(IInvoker invoker, Invocation invocation);
}