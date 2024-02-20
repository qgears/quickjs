package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;

import hu.qgears.quickjs.qpage.example.QPageContext;

public class SessionByParameterSetup extends HandlerCollection
{
	Handler delegate;
	private String sessionParameterName;

	public SessionByParameterSetup(Handler delegate, String sessionParameterName) {
		super();
		this.sessionParameterName=sessionParameterName;
		this.delegate = delegate;
		addHandler(delegate);
	}
	@Override
	public void handle(String arg0, Request arg1, HttpServletRequest arg2, HttpServletResponse arg3)
			throws IOException, ServletException {
		arg1.setRequestedSessionId(arg1.getParameter(sessionParameterName));
		delegate.handle(arg0, arg1, arg2, arg3);
	}
	public static SessionHandler setup(Server server, HandlerContainer host, QPageContext qpc) {
		DefaultSessionIdManager idmanager = new DefaultSessionIdManager(server, new SecureRandom());
		server.setSessionIdManager(idmanager);

//		// Sessions are bound to a context.
		ContextHandler context = new ContextHandler("/");
//
//		// Create the SessionHandler (wrapper) to handle the sessions
		SessionHandler sessions = new SessionHandler();
		sessions.setSessionCookie(null);
		sessions.setUsingCookies(false);
		qpc.setSessionIdParameter(sessions.getSessionIdPathParameterName());
//		sessions.setSameSite(SameSite.STRICT);
//		sessions.getSessionCookieConfig().setPath("/");
		sessions.addEventListener(HttpSessionQPageManager.createSessionListener());

		SessionByParameterSetup ret=new SessionByParameterSetup(context, sessions.getSessionIdPathParameterName());
		host.setHandler(ret);
		context.setHandler(sessions);
		return sessions;
	}
}
