package hu.qgears.quickjs.qpage.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;

import hu.qgears.quickjs.utils.HttpSessionQPageManager;
import quickjs.HelloWorld;

/**
 * Executable main class that opens a Jetty web server and handles QPage based web applications
 * within it.
 */
public class QExampleMain extends AbstractHandler
{
	byte[] staticreply="Hello!".getBytes(StandardCharsets.UTF_8);

	public static void main(String[] args) throws Exception {
		InetSocketAddress sa=new InetSocketAddress("127.0.0.1", 8888);
		Server server = new Server(sa);
		
        // Specify the Session ID Manager
        HashSessionIdManager idmanager = new HashSessionIdManager();
        server.setSessionIdManager(idmanager);

        // Sessions are bound to a context.
        ContextHandler context = new ContextHandler("/");
        server.setHandler(context);

        // Create the SessionHandler (wrapper) to handle the sessions
        HashSessionManager manager = new HashSessionManager();
        SessionHandler sessions = new SessionHandler(manager);
        sessions.addEventListener(HttpSessionQPageManager.createSessionListener());
        context.setHandler(sessions);
        sessions.setHandler(new QExampleMain());
        server.start();
        server.join();
	}

	@Override
	public void handle(String target, final Request baseRequest, HttpServletRequest request, 
			final HttpServletResponse response)
			throws IOException, ServletException {
		switch(target)
		{
		case "/":
//			response.sendRedirect("/index");
//			baseRequest.setHandled(true);
//			break;
//		case "/index":
			new QPageHandler(Index.class).handle(target, baseRequest, request, response);
			break;
		case "/01":
			new QPageHandler(QExample01.class).handle(target, baseRequest, request, response);
			break;
		case "/02":
			new QPageHandler(QExample02.class).handle(target, baseRequest, request, response);
			break;
		case "/03":
			new QPageHandler(QExample03.class).handle(target, baseRequest, request, response);
			break;
		case "/performance":
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(staticreply);
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			break;
		default:
			HelloWorld.handle(target, baseRequest, request, response);
			break;
		}
	}
}