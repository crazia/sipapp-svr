package com.comjuck.sipapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Auth {
  private static final String REALM = "comjukc.com" ;
  private static final String ALGORITHM  = "MD5" ;
  
  public enum KEY {
		RESPONSE,
		USER_NAME,
		REALM,
		CNONCE,
		NONCE,
		DIGEST,
		QOP,
		NC,
		PASSWORD;
		
		public String toString()
		{
			switch	(this)
			{
				case RESPONSE : return "response"; 
				case USER_NAME : return "username";
				case REALM : return "realm";
				case CNONCE : return "cnonce";
				case NONCE : return "nonce";
				case DIGEST : return "uri";
				case QOP : return "qop";
				case NC : return "nc";
				case PASSWORD : return "passwd";
			}
			
			return null;
		}
  }

  public static String encryptString(String param) {
    StringBuffer md5 = new StringBuffer();

    try {
      byte[] digest = java.security.MessageDigest.getInstance(ALGORITHM).digest(param.getBytes());

      for (int i = 0; i < digest.length ; i++) {
        md5.append(Integer.toString((digest[i] & 0xf0) >> 4, 16));
        md5.append(Integer.toString(digest[i] & 0x0f, 16));
      }
      
    } catch (java.security.NoSuchAlgorithmException ne) {
      ne.printStackTrace();
    }
    return md5.toString();
  }
  
  private Map<KEY, String> itemMaps = new HashMap<KEY, String>();
  public Auth(String authorization) {
    if (authorization != null) {
      String authorizationInfo[] = authorization.split (",");

      for (int i = 0; i < authorizationInfo.length ; i++) {
        String value = null;
        String key = null;

        try {
          String temp[] = authorizationInfo[i].split("=");

          key = temp[0];
          value = temp[1].replace("\"", "");
                                              
        } catch (Exception e) {
        }

        if (key == null) {
          continue;
        }

        for(KEY k : KEY.values()) {
          if (key.indexOf(k.toString()) != -1) {
            itemMaps.put(k, value);
            break;
          }
        }
      }// end of fo statement
      
    } // end of if 
  }

  public String getItem(KEY key) {
    return itemMaps.containsKey(key) ? itemMaps.get(key) : null;
  }

  public void setItem(KEY key, String value) {
    itemMaps.put(key, value);
  }

  public String getHA1() {
    return encryptString(itemMaps.get(KEY.USER_NAME) + ":" + itemMaps.get(KEY.REALM) + ":" + itemMaps.get(KEY.PASSWORD));
  }

  public String getHA2() {
    return encryptString("REGISTER:" + itemMaps.get(KEY.DIGEST));
  }

  public boolean isEqualHA() {

    KEY responseItems[] =
      {
        KEY.NONCE,
        KEY.NC,
        KEY.CNONCE,
        KEY.QOP
      };

    StringBuilder sb = new StringBuilder();
    sb.append(getHA1() + ":");
    for(KEY k : responseItems) {
      sb.append(itemMaps.get(k) + ":");
    }

    sb.append(getHA2());

    String serverHA = encryptString(sb.toString());
    String response = itemMaps.get(KEY.RESPONSE);


    return response != null && response.equals(serverHA) ? true : false;
  }

  public static String getNonce() {
    Calendar cal = Calendar.getInstance();
		java.util.Date currentTime = cal.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT);

		String nonce = encryptString(formatter.format(currentTime));
		return nonce;
  }


  public static final String getAuthorization() {
    String wwwAuthenticate = 
      "Digest realm=\"" + REALM + "\", " +
      "qop=\"auth\", " +
      "nonce=\"" + getNonce() + "\", " +
      "algorithm=\"" + ALGORITHM + "\"";
	    
		return wwwAuthenticate; 
  }

  public static final boolean isValidAuthorization(String authorization)
	{
		Auth auth	= new Auth(authorization);
		
		if	(auth.getItem(KEY.NONCE) == null || auth.getItem(KEY.NONCE).trim().equals(""))
			return	false;
		else
			return	true;
	}  
}
