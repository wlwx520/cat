package com.track.cat.handler.interfaces;

import com.track.cat.handler.Invocation;
import com.track.cat.handler.Result;

public interface Invoker {
	public Result invoke(Invocation invocation);
}
