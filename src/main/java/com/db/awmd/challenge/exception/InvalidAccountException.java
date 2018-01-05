package com.db.awmd.challenge.exception;

@SuppressWarnings("serial")
public class InvalidAccountException extends Exception {
	
	public InvalidAccountException(){
		
	}
	
	public InvalidAccountException(String message){
		super(message);
	}

}
