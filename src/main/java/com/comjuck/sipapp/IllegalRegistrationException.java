package com.comjuck.sipapp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class IllegalRegistrationException extends Exception {
  private static Log logger = LogFactory.getLog(IllegalRegistrationException.class);

  public IllegalRegistrationException	(String message)
	{
		super(message);
    logger.info("IllegalRegistrationException.. " + message);
	}

	@Override
	public String getMessage() {
		return "SIP Exception: " + super.getMessage();
	}
}
