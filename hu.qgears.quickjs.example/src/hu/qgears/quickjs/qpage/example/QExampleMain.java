package hu.qgears.quickjs.qpage.example;

import java.io.File;
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
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;

import hu.qgears.quickjs.upload.UploadHandler;
import hu.qgears.quickjs.utils.HttpSessionQPageManager;
import joptsimple.annot.AnnotatedClass;
import quickjs.HelloWorld;

/**
 * Executable main class that opens a Jetty web server and handles QPage based
 * web applications within it.
 */
public class QExampleMain extends AbstractHandler {
	byte[] staticreply = "Hello!".getBytes(StandardCharsets.UTF_8);
	private Args clargs;
	public static class Args
	{
		public String host="127.0.0.1";
		public int port=8888;
		public File uploadFolder=null;
	}

	public QExampleMain(Args clargs) {
		this.clargs=clargs;
	}

	public static void main(String[] args) throws Exception {
		
		Args clargs = new Args();
		AnnotatedClass cl = new AnnotatedClass();
		cl.parseAnnotations(clargs);
		System.out.println("QuickJS example demo program. Usage:\n");
		cl.printHelpOn(System.out);
		cl.parseArgs(args);
		
		InetSocketAddress sa = new InetSocketAddress(clargs.host, clargs.port);
		Server server = new Server(sa);

		// Specify the Session ID Manager
		DefaultSessionIdManager sessionIdManager = new DefaultSessionIdManager(server);
		server.setSessionIdManager(sessionIdManager);

		// Sessions are bound to a context.
		ContextHandler context = new ContextHandler("/");
		server.setHandler(context);

		// Create the SessionHandler (wrapper) to handle the sessions
		SessionHandler sessions = new SessionHandler();
		sessions.setSessionIdManager(sessionIdManager);
		sessions.addEventListener(HttpSessionQPageManager.createSessionListener());
		context.setHandler(sessions);
		sessions.setHandler(new QExampleMain(clargs));
		server.start();
		server.join();
	}

	@Override
	public void handle(String target, final Request baseRequest, HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ServletException {
		switch (target) {
		case "/":
			// response.sendRedirect("/index");
			// baseRequest.setHandled(true);
			// break;
			// case "/index":
			new QPageHandler(Index.class).handle(target, baseRequest, request, response, null);
			break;
		case "/01":
			new QPageHandler(QExample01.class).handle(target, baseRequest, request, response, null);
			break;
		case "/02":
			new QPageHandler(QExample02.class).handle(target, baseRequest, request, response, null);
			break;
		case "/03":
			new QPageHandler(QExample03.class).handle(target, baseRequest, request, response, null);
			break;
		case "/performance":
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(staticreply);
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			break;
		default:
			if(clargs.uploadFolder!=null && target.startsWith("/upload/"))
			{
				baseRequest.setPathInfo(target.substring("/upload".length()));
				new UploadHandler(clargs.uploadFolder).handle(baseRequest, response);
				return;
			}
			HelloWorld.handle(target, baseRequest, request, response);
			break;
		}
	}
}
