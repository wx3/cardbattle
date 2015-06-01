package com.wx3.cardbattle.datastore;

/**
 * Thrown in the event of an authentication problem
 * 
 * @author Kevin
 *
 */
public class AuthenticationException extends Exception {

	private static final long serialVersionUID = 6004L;
	
	public static final String NO_TOKEN = "NO_TOKEN";
	public static final String BAD_TOKEN = "BAD_TOKEN";
	public static final String MISSING_GAME = "MISSING_GAME";
	public static final String UNKNOWN = "UNKNOWN_ERROR";
	
	private String code;
	
	public AuthenticationException(String error) {
		super("Authentication Exception: " + error);
		this.code = error;
	}
	
	public AuthenticationException(String error, Throwable cause) {
		super("Authentication Exception: " + error, cause);
		this.code = error;
	}
	
	public String getCode() {
		return code;
	}
	
}
