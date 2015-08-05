package com.comjuck.sipapp;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.URI;


public class SipUtil {
  public String getUser (URI uri) {
    return getUser(uri.toString());
  }

  public String getUser (String sipUri) {
    int schemeIndex = sipUri.indexOf(":");
    int atIndex = sipUri.indexOf("@");
		
		return	sipUri.substring(schemeIndex + 1, atIndex);
  }

  public String getHost (URI uri) {
    return	getHost(uri.toString());
  }

  public String getHost (String sipUri) {
    
		String host		= new String(sipUri);
		int atIndex		= sipUri.indexOf("@");
		int portIndex	= sipUri.lastIndexOf(":"); 
		int paramIndex	= sipUri.lastIndexOf(";");

		if	(paramIndex > 0)
			host	= sipUri.substring(0, paramIndex);
		if	((portIndex > 0) && (portIndex > atIndex))
			host	= host.substring(0, portIndex);

		return	host.substring(atIndex + 1);
  }

	public String getParams	(URI uri)
	{
		return	getParams(uri.toString());
	}
	
	public String getParams	(String sipUri)
	{
		int paramIndex	= sipUri.indexOf(';');
		
		return	sipUri.substring(paramIndex + 1, sipUri.length());
	}

	public URI getRemoteUri	(SipFactory sf, URI contact, String remoteAddress, int remotePort, String params) throws ServletParseException
	{
		String address	= contact.getScheme() + ":" + getUser(contact) + "@" + remoteAddress + ":" + remotePort + ";" + params;
		
		return	sf.createURI(address);
	}
  
  
}
