package com.comjuck.sipapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;



public class RegistrationInfoHandler {
  private static Log logger = LogFactory.getLog(RegistrationInfoHandler.class);

  private String nvl (String str) {
    if (str == null) 
      return "" ;
    return str;
  }


  public boolean addRegistrationInfo	(JedisPool jedisPool, RegistrationInfo registrationInfo) {
    boolean	result	= true;
		Jedis jedis	= null;
		
		try	{
			// get jedis instance from pool
			jedis	= jedisPool.getResource();
			jedis.select(WebEnv.getRegistrarDBIndex());
			
			// add User-ID:GUID mapping
			{
				String deviceListKey	= Constants.RK_DEVICE_LIST + Constants.RK_DELIMITER + registrationInfo.getAor();
				
				jedis.sadd(deviceListKey, registrationInfo.getContact().toString());
				jedis.expire(deviceListKey, registrationInfo.getExpires());
			}

			// set given registrationInfo object
			{
				String registrarKey	= Constants.RK_REGISTRAR + Constants.RK_DELIMITER + registrationInfo.getContact().toString();
				

        jedis.hset(registrarKey , Constants.RF_REMOTE_ADDRESS, nvl(registrationInfo.getRemoteAddress()));
        jedis.hset(registrarKey , Constants.RF_REMOTE_PORT, nvl(Integer.toString(registrationInfo.getRemotePort())));
        jedis.hset(registrarKey , Constants.RF_PARAMS, nvl(registrationInfo.getParams()));
        jedis.hset(registrarKey, Constants.RF_USER_AGENT, nvl(registrationInfo.getUserAgent()));
        jedis.hset(registrarKey, Constants.RF_DISPLAY_NAME, nvl(registrationInfo.getDisplayName()));
        jedis.hset(registrarKey, Constants.RF_GUID, nvl(registrationInfo.getGuid()));
        jedis.hset(registrarKey, Constants.RF_NETWORK_TYPE, nvl(registrationInfo.getNetworkType()));
        jedis.hset(registrarKey, Constants.RF_CALL_ID, nvl(registrationInfo.getCallID()));
        jedis.hset(registrarKey, Constants.RF_CSEQ, nvl(registrationInfo.getcSeq()));
        jedis.hset(registrarKey, Constants.RF_EXPIRES, nvl(Integer.toString(registrationInfo.getExpires())));
        jedis.hset(registrarKey, Constants.RF_LOCATION, nvl(registrationInfo.getLocation()));

	
				DateFormat dateFormat	= new SimpleDateFormat(Constants.DATE_FORMAT);
				jedis.hset(registrarKey, Constants.RF_REGISTRATION_DTIME, dateFormat.format(registrationInfo.getRegistrationDTime()));
				jedis.expire(registrarKey, registrationInfo.getExpires());
			}
		} catch	(Exception e){
			logger.error(e);
			result	= false;
		} finally	{
			// return jedis instance
			jedisPool.returnResource(jedis);
		}
	
		return	result;
  }


  public RegistrationInfo getRegistrationInfo	(SipFactory sf, JedisPool jedisPool, URI aor, URI contact)
	{
		Vector<RegistrationInfo> registrationInfos	= getRegistrationInfos(sf, jedisPool, aor);
		RegistrationInfo registrationInfo			= null;
		
		for	(int i=0; i<registrationInfos.size(); i++){
			registrationInfo	= registrationInfos.get(i);
			
			if	(registrationInfo.getContact().equals(contact))
				break;
		}
		
		return	registrationInfo;
	}


public Vector<RegistrationInfo> getRegistrationInfos	(SipFactory sf, JedisPool jedisPool, URI aor)
	{
		Jedis jedis	= null;
		Vector<RegistrationInfo> registrationInfos	= new Vector<RegistrationInfo>();
		
		try	{
			// get jedis instance from pool
			jedis	= jedisPool.getResource();
			jedis.select(WebEnv.getRegistrarDBIndex());
			
			String deviceListKey	= Constants.RK_DEVICE_LIST + Constants.RK_DELIMITER + aor.toString();
			Set<String> contacts	= jedis.smembers(deviceListKey);
			Iterator<String> iterator	= contacts.iterator();
			
			while	(iterator.hasNext()){
				String contactStr	= iterator.next();
				String registrarKey	= Constants.RK_REGISTRAR + Constants.RK_DELIMITER + contactStr;
				Map<String, String> values	= jedis.hgetAll(registrarKey);
				
				URI contact	= sf.createURI(contactStr);
				
				DateFormat dateFormat	= new SimpleDateFormat(Constants.DATE_FORMAT);
				Date registrationDtime	= dateFormat.parse(values.get(Constants.RF_REGISTRATION_DTIME));
				
				RegistrationInfo registrationInfo	= new RegistrationInfo	(
																aor,
																contact,
																values.get(Constants.RF_REMOTE_ADDRESS), 
																Integer.parseInt(values.get(Constants.RF_REMOTE_PORT)),
																values.get(Constants.RF_PARAMS),
																values.get(Constants.RF_USER_AGENT), 
																values.get(Constants.RF_DISPLAY_NAME),
																values.get(Constants.RF_GUID), 
																values.get(Constants.RF_NETWORK_TYPE),
																values.get(Constants.RF_CALL_ID),
																values.get(Constants.RF_CSEQ),
																Integer.parseInt(values.get(Constants.RF_EXPIRES)),
																values.get(Constants.RF_LOCATION),
																registrationDtime
															);
				
				// add to vector object
				registrationInfos.add(registrationInfo);
			}
		} catch (Exception e){
			logger.error(e);
		} finally	{
			// return jedis instance
			jedisPool.returnResource(jedis);
		}
			return	registrationInfos;
	}

	public Vector<URI> getContacts	(SipFactory sf, JedisPool jedisPool, URI aor)
	{
		Jedis jedis	= null;
		Vector<URI> contacts	= new Vector<URI>();
		
		try	{
			// get jedis instance from pool
			jedis	= jedisPool.getResource();
			jedis.select(WebEnv.getRegistrarDBIndex());
			
			String deviceListKey	= Constants.RK_DEVICE_LIST + Constants.RK_DELIMITER + aor.toString();
			Set<String> values	= jedis.smembers(deviceListKey);
			Iterator<String> iterator	= values.iterator();
			
			while	(iterator.hasNext()){
				contacts.add(sf.createURI(iterator.next()));
			}
		} catch (Exception e){
			
		} finally	{
			// return jedis instance
			jedisPool.returnResource(jedis);
		}
		
		return	contacts;
	}
  
  public Vector<URI> getRemoteUris	(SipFactory sf, JedisPool jedisPool, URI aor)
	{
		Jedis jedis	= null;
		Vector<URI> remoteUris	= new Vector<URI>();
		SipUtil sipUtil	= new SipUtil();
		
		try	{
			// get jedis instance from pool
			jedis	= jedisPool.getResource();
			jedis.select(WebEnv.getRegistrarDBIndex());
			
			String deviceListKey	= Constants.RK_DEVICE_LIST + Constants.RK_DELIMITER + aor.toString();
			Set<String> contacts	= jedis.smembers(deviceListKey);
			Iterator<String> iterator	= contacts.iterator();
			
			while	(iterator.hasNext()){
				String contactStr	= iterator.next();
				String registrarKey	= Constants.RK_REGISTRAR + Constants.RK_DELIMITER + contactStr;
				Map<String, String> values	= jedis.hgetAll(registrarKey);
				
				URI contact	= sf.createURI(contactStr);
				String remoteAddress	= values.get(Constants.RF_REMOTE_ADDRESS);
				int remotePort			= Integer.parseInt(values.get(Constants.RF_REMOTE_PORT));
				String params			= values.get(Constants.RF_PARAMS);
				URI remoteUri			= sipUtil.getRemoteUri(sf, contact, remoteAddress, remotePort, params);
				
				// add to vector object
				remoteUris.add(remoteUri);
			}
		} catch (Exception e){
			logger.error(e);
		} finally	{
			// return jedis instance
			jedisPool.returnResource(jedis);
		}
		
		return	remoteUris;
	}

	public void removeRegistrationInfo	(JedisPool jedisPool, URI aor, URI contact)
	{
		Jedis jedis	= null;
		
		try	{
			// get jedis instance from pool
			jedis	= jedisPool.getResource();
			jedis.select(WebEnv.getRegistrarDBIndex());
			
			// remove DEVICE_LIST mapping
			{
				String deviceListKey	= Constants.RK_DEVICE_LIST + Constants.RK_DELIMITER + aor.toString();
				jedis.srem(deviceListKey, contact.toString());
			}
			
			// remove REGISTRATION by aor and contact
			{
				String registrarKey	= Constants.RK_REGISTRAR + Constants.RK_DELIMITER + contact.toString();
				
				jedis.hdel(registrarKey, Constants.RF_REMOTE_ADDRESS);
				jedis.hdel(registrarKey, Constants.RF_REMOTE_PORT);
				jedis.hdel(registrarKey, Constants.RF_PARAMS);
				jedis.hdel(registrarKey, Constants.RF_USER_AGENT);
				jedis.hdel(registrarKey, Constants.RF_DISPLAY_NAME);
				jedis.hdel(registrarKey, Constants.RF_GUID);
				jedis.hdel(registrarKey, Constants.RF_NETWORK_TYPE);
				jedis.hdel(registrarKey, Constants.RF_CALL_ID);
				jedis.hdel(registrarKey, Constants.RF_CSEQ);
				jedis.hdel(registrarKey, Constants.RF_EXPIRES);
				jedis.hdel(registrarKey, Constants.RF_LOCATION);
				jedis.hdel(registrarKey, Constants.RF_REGISTRATION_DTIME);
			}
		} catch	(Exception e){
			logger.error(e);
		} finally	{
			// return jedis instance
			jedisPool.returnResource(jedis);
		}
		
		return;
	}
  
public Vector<RegistrationInfo> dumpRegistrar	(SipFactory sf, JedisPool jedisPool)
	{
		Jedis jedis	= null;
		Vector<RegistrationInfo> registrationInfos	= new Vector<RegistrationInfo>();
		
		try	{
			// get jedis instance from pool
			jedis	= jedisPool.getResource();
			jedis.select(WebEnv.getRegistrarDBIndex());

			Set<String> aors		= jedis.keys(Constants.RK_DEVICE_LIST + Constants.RK_DELIMITER + "*");
			Iterator<String> iterator	= aors.iterator();
			
			while	(iterator.hasNext()){
				String aor	= iterator.next();
				aor	= aor.substring((Constants.RK_DEVICE_LIST + Constants.RK_DELIMITER).length(), aor.length());
				Vector<RegistrationInfo> registrationInfoByAor	= getRegistrationInfos(sf, jedisPool, sf.createURI(aor));
				
				for	(int i=0; i<registrationInfoByAor.size(); i++){
					registrationInfos.add(registrationInfoByAor.get(i));
				}
			}
		} catch (Exception e){
			logger.error(e);
		} finally	{
			// return jedis instance
			jedisPool.returnResource(jedis);
		}
		
		return	registrationInfos;
	}  


  public void	flushRegistrar	(JedisPool jedisPool)
	{
		Jedis jedis	= null;
		
		try	{
			// get jedis instance from pool
			jedis	= jedisPool.getResource();
			jedis.select(WebEnv.getRegistrarDBIndex());

			jedis.flushDB();
		} catch	(Exception e){
			logger.error(e);
		} finally	{
			// return jedis instance
			jedisPool.returnResource(jedis);
		}
		
		return;
	}  
  
}

