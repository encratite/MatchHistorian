package com.github.epicvrvs.matchhistorian;

class MatchHistorianException extends Exception {
	public MatchHistorianException(String message) {
		super(message);
	}
	
	public MatchHistorianException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public MatchHistorianException(Throwable throwable) {
		super(throwable);
	}
}
