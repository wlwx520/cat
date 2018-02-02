package com.track.paint.core.exception;

public class SystemException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3198906060656330411L;

	public SystemException(String msg) {
		super(msg);
	}

	public SystemException(Throwable throwable) {
		super(throwable);
	}

	public SystemException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

}
