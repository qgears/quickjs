package hu.qgears.quickjs.utils.gdpr;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

/**
 * Session ID manager that allows session free access to the server
 * so that cookies are not stored by default.
 * Necessary according to "new" EU regulations.
 */
public class GdprSessionHandler extends HandlerWrapper
{
	protected String sessionCookieName;
	protected String cookiesAcceptedCookieName;
	private String path="/";
	private GdprSessionIdManager idmanager;
	
	public GdprSessionHandler(GdprSessionIdManager idmanager, String sessionCookieName) {
		super();
		this.idmanager=idmanager;
		idmanager.handler=this;
		this.sessionCookieName = sessionCookieName;
		this.cookiesAcceptedCookieName=sessionCookieName+"_cookiesAccepted";
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		Cookie[] cookies=baseRequest.getCookies();
		boolean cookiesAccepted=false;
		boolean cookieSet=false;
		String sessionCookieId=null;
		String sessionIdFromRequest=null;
		if(cookies!=null)
		{
			for(Cookie c: cookies)
			{
				if(sessionCookieName.equals(c.getName()))
				{
					sessionCookieId=c.getValue();
					cookieSet=true;
				}
				if(cookiesAcceptedCookieName.equals(c.getName()))
				{
					if("true".equals(c.getValue()))
					{
						cookiesAccepted=true;
					}
				}
			}
		}
		sessionIdFromRequest=sessionCookieId;
		if(sessionIdFromRequest==null)
		{
			sessionIdFromRequest=baseRequest.getParameter(GdprSession.keySessionIdParameterName);
		}
		GdprSession session=idmanager.getSession(sessionIdFromRequest, cookiesAccepted);
		cookieSet = session.getId().equals(sessionCookieId);
		if(cookiesAccepted && !cookieSet)
		{
			addCookieHeaderToResponse(response, session.getId());
		}
		session.setAttribute(GdprSession.keyUseNoCookieSession, !cookiesAccepted);
		session.cookiesAccepted=cookiesAccepted;
		baseRequest.setSession(session);
		try
		{
			super.handle(target, baseRequest, request, response);
		}finally {
			baseRequest.setSession(null);
		}
	}
	public void setCookiePath(String path) {
		this.path = path;
	}
	private void addCookieHeaderToResponse(HttpServletResponse response, String secureId)
	{
		String content=sessionCookieName+"="+secureId+"; Path="+path;
		response.addHeader("Set-Cookie", content);
	}
	public void addEventListener(HttpSessionListener createSessionListener) {
		idmanager.addEventListener(createSessionListener);
	}
}
