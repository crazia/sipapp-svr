package com.comjuck.sipapp;

import redis.clients.jedis.JedisPool;

public class WebEnv {
  public static JedisPool getJedisPool() {
    return WebEnvInit.jedisPool;
  }

  public static String getParameter(String name) {
    return (String ) WebEnvInit.hs.get(name);
  }

  public static void setParameter(String name, String value) {
    WebEnvInit.hs.remove(name);
    WebEnvInit.hs.put(name, value);
  }

  public static String getRedisServerIpAddress() {
    return getParameter("RedisServerIpAddress");
  }

  public static int getRedisServerPort() {
    return Integer.parseInt(getParameter("RedisServerPort"));
  }

public static int getRegistrarDBIndex() 
	{
		return Integer.parseInt(getParameter("RegistrarDBIndex"));
	}

	public static int getDefaultExpires() 
	{
		return Integer.parseInt(getParameter("DefaultExpires"));
	}  
}

