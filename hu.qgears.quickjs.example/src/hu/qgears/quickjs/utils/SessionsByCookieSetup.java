package hu.qgears.quickjs.utils;

import java.security.SecureRandom;

import org.eclipse.jetty.http.HttpCookie.SameSite;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;

public class SessionsByCookieSetup {
	public static SessionHandler setup(Server s, HandlerContainer host, String cookieName)
	{
		// Specify the Session ID Manager
		DefaultSessionIdManager idmanager = new DefaultSessionIdManager(s, new SecureRandom());
		s.setSessionIdManager(idmanager);

		// Sessions are bound to a context.
		ContextHandler context = new ContextHandler("/");
		host.setHandler(context);

		// Create the SessionHandler (wrapper) to handle the sessions
		SessionHandler sessions = new SessionHandler();
		sessions.setSessionCookie(cookieName);
		sessions.setSameSite(SameSite.STRICT);
		sessions.getSessionCookieConfig().setPath("/");
		// One year - should be enough
		sessions.getSessionCookieConfig().setMaxAge(60*60*24*365);
		// TODO maybe secure cookie config should be used?
		// sessions.getSessionCookieConfig().setSecure(true);
		sessions.addEventListener(HttpSessionQPageManager.createSessionListener());
		context.setHandler(sessions);
		return sessions;
	}
}
