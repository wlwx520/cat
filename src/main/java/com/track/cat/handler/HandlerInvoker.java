package com.track.cat.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.track.cat.handler.interfaces.Invoker;

public class HandlerInvoker implements Invoker {
	private Method method;
	private Object instance;

	public HandlerInvoker(Method method, Object instance) {
		this.method = method;
		this.instance = instance;
	}

	@Override
	public Result invoke(Invocation invocation) {
		try {
			return (Result) method.invoke(instance, invocation);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
