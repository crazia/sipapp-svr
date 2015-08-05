package com.comjuck.sipapp;

import java.util.Date;
import javax.servlet.sip.URI;

public class RegistrationInfo {
  private	URI		aor					= null;
	private URI		contact				= null;
	private String	remoteAddress		= null;
	private int		remotePort			= 0;
	private String	params				= null;
	private String	userAgent			= null;
	private	String	displayName			= null;
	private String	guid				= null;
	private String	networkType			= null;
	private String	callID				= null;
	private String	cSeq				= null;
	private int		expires				= 0;
	private String	location			= null;
	private Date	registrationDTime	= null;



  	public	RegistrationInfo	(
									URI	_aor, 
									URI	_contact,
									String	_remoteAddress,
									int		_remotePort,
									String	_params,
									String	_userAgent,
									String	_displayName, 
									String	_guid,
									String	_networkType,
									String	_callID, 
									String		_cSeq, 
									int		_expires, 
									String	_location,
									Date	_registrationDTime
								)
								throws IllegalRegistrationException
	{
		this.aor				= _aor;
		this.contact			= _contact;
		this.remoteAddress		= _remoteAddress;
		this.remotePort			= _remotePort;
		this.params				= _params;
		this.userAgent			= _userAgent;
		this.displayName		= _displayName;
		this.guid				= _guid;
		this.networkType		= _networkType;
		this.callID				= _callID;
		this.cSeq				= _cSeq;
		this.expires			= _expires;
		this.location			= _location;
		this.registrationDTime	= _registrationDTime;

		if	(this.aor == null)
			throw new IllegalRegistrationException("AOR cannot be null as this is a key.");

		if	(this.contact == null)
			throw new IllegalRegistrationException("Contact cannot be null.");

		if	(this.guid == null)
			this.guid			= "";
		
		if	(this.networkType == null)
			this.networkType	= "";

		if	(this.displayName == null)
			this.displayName	= "";
		
		if	(this.expires < 0)
			this.expires		= WebEnv.getDefaultExpires();
	}

public String toString	()
	{
		String	value	= "[ " 
						+ Constants.RF_AOR + "=" + this.aor.toString() + ", "
						+ Constants.RF_CONTACT + "=" + this.contact.toString() + ", "
						+ Constants.RF_REMOTE_ADDRESS + "=" + this.remoteAddress + ", "
						+ Constants.RF_REMOTE_PORT + "=" + this.remotePort + ", "
						+ Constants.RF_PARAMS + "=" + this.params + ", "
						+ Constants.RF_USER_AGENT + "=" + this.userAgent + ", "
						+ Constants.RF_DISPLAY_NAME + "=" + this.displayName + ", "
						+ Constants.RF_GUID + "=" + this.guid + ", "
						+ Constants.RF_NETWORK_TYPE + "=" + this.networkType + ", "
						+ Constants.RF_CALL_ID + "=" + this.callID + ", "
						+ Constants.RF_CSEQ + "=" + this.cSeq + ", "
						+ Constants.RF_EXPIRES + "=" + this.expires + ", "
						+ Constants.RF_LOCATION + "=" + this.location + ", "
						+ Constants.RF_REGISTRATION_DTIME + "=" + this.registrationDTime.toString()
						+ "]";

		return	value;
	}


	// getters
	public URI getAor() {
		return aor;
	}

	public URI getContact() {
		return contact;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public int getRemotePort() {
		return remotePort;
	}

	public String getParams() {
		return params;
	}
	
	public String getUserAgent() {
		return userAgent;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getGuid() {
		return guid;
	}

	public String getNetworkType() {
		return networkType;
	}

	public String getCallID() {
		return callID;
	}

	public String getcSeq() {
		return cSeq;
	}

	public int getExpires() {
		return expires;
	}

	public String getLocation() {
		return location;
	}

	public Date getRegistrationDTime() {
		return registrationDTime;
	}  
  
}
  
