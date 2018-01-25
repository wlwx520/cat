package com.track.cat.core;

import com.track.cat.core.interfaces.IInvoker;

public class BaseHandler {
	private HttpMethod[] methods;
	private IInvoker invoker;

	public BaseHandler(HttpMethod[] methods, IInvoker invoker) {
		this.methods = methods;
		this.invoker = invoker;
	}

	public HttpMethod[] getMethods() {
		return methods;
	}

	public IInvoker getInvoker() {
		return invoker;
	}

}
