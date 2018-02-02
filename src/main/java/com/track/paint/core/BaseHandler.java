package com.track.paint.core;

import com.track.paint.core.interfaces1.IInvoker;

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
