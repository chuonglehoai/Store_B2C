package com.salesmanager.shop.api.exception;

public class UserAlreadyExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public UserAlreadyExistException(String message) {
		super(message,null);
	}

}
