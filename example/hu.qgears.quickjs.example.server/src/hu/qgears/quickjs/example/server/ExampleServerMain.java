package hu.qgears.quickjs.example.server;

import java.net.InetSocketAddress;
import java.security.SecureRandom;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;

import hu.qgears.quickjs.example.gui.ExampleGui;
import hu.qgears.quickjs.qpage.jetty.QPageHandler;
import hu.qgears.quickjs.spa.QSpa;
import hu.qgears.quickjs.utils.DispatchHandler;
import hu.qgears.quickjs.utils.HttpSessionQPageManager;
import joptsimple.annot.AnnotatedClass;

/** Entry point to launch a server containing the example web application with embedded Jetty. */
public class ExampleServerMain {
	public static class Args
	{
		public String host="127.0.0.1";
		public int port=8888;
	}
//	ServletContextHandler ws;
	private Args clargs;
	private Server server;
	public ExampleServerMain(Args clargs) {
		this.clargs=clargs;
//		ws=QWSMessagingServlet.createHandler();
	}

	public static void main(String[] args) throws Exception {
		Args clargs = new Args();
		AnnotatedClass cl = new AnnotatedClass();
		cl.parseAnnotations(clargs);
		System.out.println("QuickJS example demo program. Usage:\n");
		cl.printHelpOn(System.out);
		cl.parseArgs(args);
		new ExampleServerMain(clargs).launch();
	}

	private void launch() throws Exception {
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
		QSpa spa=new ExampleGui().createApplication();
		dh.addHandler("/", new QPageHandler(spa));
		sessions.setHandler(dh);
		// ws.setServer(server);
		// ws.start();
		server.start();
		server.join();
	}

}
