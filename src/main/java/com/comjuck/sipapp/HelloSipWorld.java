/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.comjuck.sipapp;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This example shows a typical UAS and reply 200 OK to any INVITE or BYE it receives
 * 
 * @author Jean Deruelle
 *
 */
public class HelloSipWorld extends SipServlet {

	private static Log logger = LogFactory.getLog(HelloSipWorld.class);

  public static final String	REMOTE_URI			= "REMOTE_URI";

  @Resource
	SipFactory		sf;
  
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the HelloSipWorld servlet has been started");

		super.init(servletConfig);
	}

  @Override
  protected void doRegister(SipServletRequest request) throws ServletException, IOException
  {
    logger.debug("doRegister() invoked");

    String authorization = request.getHeader(Constants.SH_AUTHORIZATION);
    if (Auth.isValidAuthorization(authorization) == false) {
      SipServletResponse response	= request.createResponse(SipServletResponse.SC_UNAUTHORIZED);
			response.addHeader(Constants.SH_WWW_AUTHENTICATE, Auth.getAuthorization());
			response.send();
			logger.info("401 Unauthorized response sent.");
    } else {
      Auth auth		= new Auth(authorization);

      try {
        if (isFetch(request)) {
          logger.info("REGISTRATION FETCH REQUEST");
          SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
          sipServletResponse.send();
        } else {
          
          URI aor	= request.getTo().getURI();
          URI contact	= request.getAddressHeader(Constants.SH_CONTACT).getURI();

          RegistrationInfoHandler handler	= new RegistrationInfoHandler();

          if (isUnregistration(request)) {
            logger.info("unregistered request");

            handler.removeRegistrationInfo(WebEnv.getJedisPool(), aor, contact);
            SipServletResponse response	= request.createResponse(SipServletResponse.SC_OK);
            response.send();
          } else {
            SipUtil sipUtil	= new SipUtil();
            RegistrationInfo registrationInfo	= new RegistrationInfo	(
																							aor,
																							contact,
																							request.getRemoteAddr(),
																							request.getRemotePort(),
																							sipUtil.getParams(contact.toString()),
																							request.getHeader(Constants.SH_USER_AGENT),
																							request.getTo().getDisplayName(),
																							request.getHeader(Constants.SH_GUID),
																							request.getHeader(Constants.SH_NETWORK_TYPE),
																							request.getCallId(),
																							request.getHeader(Constants.SH_CSEQ),
																							request.getExpires(),
																							"KR",
																							new Date()
																						);

            handler.addRegistrationInfo(WebEnv.getJedisPool(), registrationInfo);
            logger.info("-- RegistrationInfo Stored --");
            logger.info(registrationInfo.toString());
            logger.info("-----------------------------");            

            SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
            if	(request.getExpires() == -1)
              response.setExpires(WebEnv.getDefaultExpires());
            response.send();
            logger.info("200 OK response sent. Successfully stored device's location info.");

            request.getApplicationSession().setExpires(registrationInfo.getExpires()/60);
            logger.info("SipApplicationSession expires in: " + new Date(request.getApplicationSession().getExpirationTime()).toString());
            
          }
        }

      } catch (Exception ex) {
        SipServletResponse response	= request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, ex.getMessage());
					response.send();
					logger.info("500 Internal Server Error response sent.");        
      }
    }
    // else {
    //   SipServletResponse response	= request.createResponse(SipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
    //   response.send();
    //   logger.info("407 Proxy Authentication Required response sent.");
    // }
  }


  @Override
  protected void doAck(SipServletRequest request) throws ServletException, IOException {
    B2buaHelper b2b = request.getB2buaHelper();
    SipSession sipSession = b2b.getLinkedSession(request.getSession());
    List<SipServletMessage> msgs = b2b.getPendingMessages(sipSession, UAMode.UAC);

    for (SipServletMessage msg : msgs) {
			if (msg instanceof SipServletResponse) {
				SipServletResponse response = (SipServletResponse) msg;

				if (response.getStatus() == SipServletResponse.SC_OK) {
					SipServletRequest ack = response.createAck();
					ack.setRequestURI((URI) sipSession.getAttribute(REMOTE_URI));
					copyContent(request, ack);
					ack.send();
					logger.info("Sent ACK out.");
				}
			}
		}
  }
  

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

    // for original code 
		// logger.info("Got request:\n"
		// 		+ request.toString());
		// String fromUri = request.getFrom().getURI().toString();
		// logger.info(fromUri);
		
		// SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		// sipServletResponse.send();


    RegistrationInfoHandler handler	= new RegistrationInfoHandler();
		B2buaHelper b2b	= request.getB2buaHelper();
		URI aorOfCaller	= request.getFrom().getURI();
		Vector<URI> remoteUrisOfCaller	= handler.getRemoteUris(sf, WebEnv.getJedisPool(), aorOfCaller);
		request.getSession().setAttribute(REMOTE_URI, remoteUrisOfCaller.get(0));

    if (request.isInitial()) {
      URI aorOfCallee	= request.getTo().getURI();
			Vector<URI> remoteUrisOfCallee	= handler.getRemoteUris(sf, WebEnv.getJedisPool(), aorOfCallee);


      if	(remoteUrisOfCallee.size() > 0){
				// OPMD is not supported yet
				SipServletRequest other	= b2b.createRequest(request, true, null);
				other.setRequestURI(remoteUrisOfCallee.get(0));
				copyContent(request, other);
				other.send();
				
				// store remote URI for later use
				other.getSession().setAttribute(REMOTE_URI, remoteUrisOfCallee.get(0));
				logger.info("Invite to " + remoteUrisOfCallee.get(0) + " has been sent!!");
			} else	{
				SipServletResponse response	= request.createResponse(SipServletResponse.SC_NOT_FOUND);
				response.send();
				
				logger.info("The callee [" + aorOfCallee.toString() + "] does not exist in Registrar!!");
			} // end of remoteUrisOfCallee.size else 
    } else {
      // RE-INVITE
			SipSession linked = b2b.getLinkedSession(request.getSession());
			SipServletRequest other = b2b.createRequest(linked, request, null);
			copyContent(request, other);
			other.send();
			logger.info("Subsequent request!" + request.getHeader("Cseq"));
    }
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		// SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		// sipServletResponse.send();

    B2buaHelper b2b	= request.getB2buaHelper();
		SipSession linked = b2b.getLinkedSession(request.getSession());
		SipServletRequest other = b2b.createRequest(linked, request, null);
		other.setRequestURI((URI) linked.getAttribute(REMOTE_URI));
		copyContent(request, other);
		other.send();
	}


  private boolean isFetch (SipServletRequest request) throws ServletParseException {
    if	((request.getAddressHeader(Constants.RF_CONTACT) == null) && (request.getExpires() <= 0))
			return	true;
		else
			return	false;
  }

	private boolean isUnregistration	(SipServletRequest request) throws ServletParseException
	{
		boolean	isUnregistration	= false;
		
		// get the Contact for this REGISTER request, assuming there is at most one.
		Address contact	= request.getAddressHeader(Constants.RF_CONTACT);
		
		if		(contact.isWildcard() || (request.getExpires() == 0))
			isUnregistration	= true;
		else if	(contact.getExpires() == 0)
			isUnregistration	= true;
		
		return	isUnregistration;
	}


  protected void copyContent (SipServletMessage source, SipServletMessage destination) throws IOException
	{
		if (source.getContentLength() > 0) {
			destination.setContent(source.getContent(), source.getContentType());
			String enc = source.getCharacterEncoding();

			if (enc != null && enc.length() > 0) {
				destination.setCharacterEncoding(enc);
			}
		}
	}

  @Override
	protected void doCancel(SipServletRequest request) throws ServletException, IOException 
	{
		B2buaHelper b2b = request.getB2buaHelper();
		SipSession sipSession = b2b.getLinkedSession(request.getSession());
		SipServletRequest cancel = b2b.createCancel(sipSession);
		cancel.setRequestURI((URI) sipSession.getAttribute(REMOTE_URI));
		cancel.send();
	}


  @Override
	protected void doResponse(SipServletResponse response) throws ServletException, IOException 
	{
		if (response.getStatus() == SipServletResponse.SC_REQUEST_TERMINATED) {
			return; // 487 already sent on Cancel for initial leg UAS
		}

		B2buaHelper b2b = response.getRequest().getB2buaHelper();
		SipSession linked = b2b.getLinkedSession(response.getSession());
		SipServletResponse other = null;

		if (response.getRequest().isInitial()) { 
			// Handled separetly due to possibility of forking and multiple 200
			other = b2b.createResponseToOriginalRequest(linked, response.getStatus(), response.getReasonPhrase());
		} else { 
			// Other responses then to initial request
			SipServletRequest otherReq = b2b.getLinkedSipServletRequest(response.getRequest());
			other = otherReq.createResponse(response.getStatus(), response.getReasonPhrase());
		}

		copyContent(response, other);
		other.send();
	}

  
  
}
