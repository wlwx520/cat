package com.track.cat.handler.exception;

public class MappingNotFoundExcption extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7946424081891236708L;

	public MappingNotFoundExcption(String msg) {
		super(msg + "is not found");
	}
}
