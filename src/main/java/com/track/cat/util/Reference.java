package com.track.cat.util;

public class Reference<T> {
	private T t;

	public Reference(T t) {
		this.t = t;
	}

	public T get() {
		return t;
	}

	public void set(T t) {
		this.t = t;
	}

}
