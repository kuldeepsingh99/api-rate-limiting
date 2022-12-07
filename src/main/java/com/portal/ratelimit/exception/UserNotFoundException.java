package com.portal.ratelimit.exception;

public class UserNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UserNotFoundException(String string) {
		super(string);
	}
}
