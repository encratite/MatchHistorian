package com.github.epicvrvs.matchhistorian;

class HTTPException extends Exception {
	public HTTPException(String message) {
		super(message);
	}
	
	public HTTPException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public HTTPException(Throwable throwable) {
		super(throwable);
	}
}
