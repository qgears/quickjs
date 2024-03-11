package hu.qgears.quickjs.utils.gdpr;

import java.security.SecureRandom;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

import hu.qgears.commons.UtilTimer;
import hu.qgears.quickjs.utils.HttpSessionQPageManager;

public class GdprSessionsSetup {
	public static GdprSessionHandler setup(Server s, String cookieName)
	{
		// Specify the Session ID Manager
		GdprSessionIdManager idmanager = new GdprSessionIdManager(s, new SecureRandom(), UtilTimer.javaTimer);
		// Sessions are bound to a context.
		ContextHandler context = new ContextHandler("/");
		s.setHandler(context);
		// Create the SessionHandler (wrapper) to handle the sessions
		GdprSessionHandler sessions = new GdprSessionHandler(idmanager, cookieName);
		sessions.addEventListener(HttpSessionQPageManager.createSessionListener());
		context.setHandler(sessions);
		return sessions;
	}
}
