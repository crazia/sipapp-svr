package com.comjuck.sipapp;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class WebEnvInit implements ServletContextListener {
  private static Log logger = LogFactory.getLog(WebEnvInit.class);

  protected static Hashtable<String , String > hs = null;

  protected static JedisPool jedisPool = null;


  public void contextInitialized(ServletContextEvent event) {
    logger.debug("WebEnvInit.contextInitialized invoked");

    if (hs == null) {
      hs = new Hashtable<String , String >();

      Enumeration params = event.getServletContext().getInitParameterNames();
      while (params.hasMoreElements()) {
        String param = (String ) params.nextElement();
        String value = event.getServletContext().getInitParameter(param);

        hs.put(param, value);
      }
    }

    if	(jedisPool == null){
			jedisPool	= new JedisPool(new JedisPoolConfig(), WebEnv.getRedisServerIpAddress(), WebEnv.getRedisServerPort());
		}
  }

  public void contextDestroyed(ServletContextEvent event) {
    logger.debug("WebEnvInit.contextDestroyed invoked");

    jedisPool.destroy();
  }
}
