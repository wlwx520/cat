package com.track.cat.core.exception;

public class CatSqlExcption extends CatSystemException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7705908944712432349L;

	public CatSqlExcption(String msg) {
		super(msg);
	}

	public CatSqlExcption(Throwable throwable) {
		super(throwable);
	}

	public CatSqlExcption(String msg, Throwable throwable) {
		super(msg, throwable);
	}

}
