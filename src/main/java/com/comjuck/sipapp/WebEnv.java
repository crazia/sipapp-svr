package com.comjuck.sipapp;

import java.util.Enumeration;
import java.util.Hashtable;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.util.Properties;

import java.io.FileNotFoundException;
import java.io.IOException;

public class WebEnv {

  protected static JedisPool jedisPool = null;
  protected static Hashtable<String , String > hs = null;

  public static JedisPool getJedisPool() {
    jedisPool = new JedisPool(new JedisPoolConfig(), getRedisServerIpAddress(),
                              getRedisServerPort());
    return jedisPool;
  }

  public static String getPropValue(String key) {
    Properties p = new Properties();

    try {
      p.load(WebEnv.class.getResourceAsStream("/config.properties"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return p.getProperty(key);
  }

//   public static String getParameter(String name) {
//     return (String ) WebEnvInit.hs.get(name);
//   }

//   public static void setParameter(String name, String value) {
//     WebEnvInit.hs.remove(name);
//     WebEnvInit.hs.put(name, value);
//   }

  public static String getRedisServerIpAddress() {
    return getPropValue("redis.ip");
  }

  public static int getRedisServerPort() {
    return Integer.parseInt(getPropValue("redis.port"));
  }

  public static int getRegistrarDBIndex() 
	{
		return Integer.parseInt(getPropValue("redis.dbindex"));
	}

	public static int getDefaultExpires() 
	{
		return Integer.parseInt(getPropValue("redis.expire"));
	}  
}

