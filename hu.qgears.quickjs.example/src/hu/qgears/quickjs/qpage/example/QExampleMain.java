package hu.qgears.quickjs.qpage.example;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import hu.qgears.quickjs.qpage.example.websocket.QWSMessagingServlet;
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
	ServletContextHandler ws;
	QPageContext qpc;
	private Server server;
	public static class Args
	{
		public String host="127.0.0.1";
		public int port=8888;
		public File uploadFolder=null;
	}

	public QExampleMain(Args clargs) {
		this.clargs=clargs;
		ws=QWSMessagingServlet.createHandler();
	}

	public static void main(String[] args) throws Exception {
		Args clargs = new Args();
		AnnotatedClass cl = new AnnotatedClass();
		cl.parseAnnotations(clargs);
		System.out.println("QuickJS example demo program. Usage:\n");
		cl.printHelpOn(System.out);
		cl.parseArgs(args);
		new QExampleMain(clargs).launch();
	}
	private void launch() throws Exception
	{
		InetSocketAddress sa = new InetSocketAddress(clargs.host, clargs.port);
		server = new Server(sa);
		qpc=new QPageContext(server);

		// Specify the Session ID Manager
		DefaultSessionIdManager idmanager = new DefaultSessionIdManager(server, new SecureRandom());
		server.setSessionIdManager(idmanager);

		// Sessions are bound to a context.
		ContextHandler context = new ContextHandler("/");
		server.setHandler(context);

		// Create the SessionHandler (wrapper) to handle the sessions
		SessionHandler sessions = new SessionHandler();
		sessions.addEventListener(HttpSessionQPageManager.createSessionListener());
		context.setHandler(sessions);
		
		HandlerList hl=new HandlerList(this,
				new HelloWorld());
		sessions.setHandler(hl);
		ws.setServer(server);
		ws.start();
		server.start();
		server.join();
	}

	@Override
	public void handle(String target, final Request baseRequest, HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ServletException {
		switch (target) {
		case "/":
			new QPageHandler(qpc, Index.class).handle(target, baseRequest, request, response);
			break;
		case "/01":
			new QPageHandler(qpc, QExample01.class).handle(target, baseRequest, request, response);
			break;
		case "/02":
			new QPageHandler(qpc, QExample02.class).handle(target, baseRequest, request, response);
			break;
		case "/03":
			new QPageHandler(qpc, QExample03.class).handle(target, baseRequest, request, response);
			break;
		case "/04":
			new QPageHandler(qpc, QExample04DynamicDivs.class).handle(target, baseRequest, request, response);
			break;
		case "/performance":
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(staticreply);
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			break;
		case "/messaging":
		case "/wsexample":
			ws.handle(target, baseRequest, request, response);
			baseRequest.setHandled(true);
			break;
		default:
			if(clargs.uploadFolder!=null && target.startsWith("/upload/"))
			{
				baseRequest.setPathInfo(target.substring("/upload".length()));
				new UploadHandler(clargs.uploadFolder).handle(baseRequest, response);
				return;
			}
			baseRequest.setHandled(true);
			// Unhandled
			break;
		}
	}
}
