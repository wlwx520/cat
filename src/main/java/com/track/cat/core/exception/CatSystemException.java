package com.track.cat.core.exception;

public class CatSystemException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3198906060656330411L;

	public CatSystemException(String msg) {
		super(msg);
	}

	public CatSystemException(Throwable throwable) {
		super(throwable);
	}

	public CatSystemException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

}
