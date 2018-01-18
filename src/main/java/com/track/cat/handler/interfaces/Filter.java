package com.track.cat.handler.interfaces;

import com.track.cat.handler.Invocation;
import com.track.cat.handler.Result;

public interface Filter {
	Result invoke(Invoker invoker, Invocation invocation);
}