package hu.qgears.quickjs.qpage.example;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;

import hu.qgears.quickjs.qpage.jetty.QPageHandler;
import hu.qgears.quickjs.qpage.jetty.websocket.QWSMessagingServlet;
import hu.qgears.quickjs.utils.DispatchHandler;
import hu.qgears.quickjs.utils.HttpSessionQPageManager;
import joptsimple.annot.AnnotatedClass;
import quickjs.HelloWorld;

/**
 * Executable main class that opens a Jetty web server and handles QPage based
 * web applications within it.
 */
public class QExampleMain {
	byte[] staticreply = "Hello!".getBytes(StandardCharsets.UTF_8);
	private Args clargs;
	AbstractHandler ws;
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
		
		DispatchHandler dh=new DispatchHandler();
		dh.addHandler("/", new QPageHandler(Index.class));
		dh.addHandler("/", "/01", new QPageHandler(QExample01.class));
		dh.addHandler("/", "/02", new QPageHandler(QExample02.class));
		dh.addHandler("/", "/03", new QPageHandler(QExample03.class));
		dh.addHandler("/", "/04", new QPageHandler(QExample04DynamicDivs.class));
		// TODO re-enable other examples
		HandlerList hl=new HandlerList(dh,
				new HelloWorld());
		sessions.setHandler(hl);
		ws.setServer(server);
		ws.start();
		server.start();
		server.join();
	}

/*	@Override
	public void handle(String target, final Request baseRequest, HttpServletRequest request,
			final HttpServletResponse response) throws IOException, ServletException {
		switch (target) {
		case "/":
			.handle(target, baseRequest, request, response);
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
	*/
}
