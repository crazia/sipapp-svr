package com.comjuck.sipapp;



public class IllegalRegistrationException extends Exception {
  public IllegalRegistrationException	(String message)
	{
		super(message);
	}

	@Override
	public String getMessage() {
		return "SIP Exception: " + super.getMessage();
	}
}
