package hu.qgears.quickjs.qpage.example.websocket;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class QWSMessagingServlet extends WebSocketServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.getPolicy().setIdleTimeout(QWSMessagingClass.pingPeriodMillis*2);
		// factory.getPolicy().setMaxBinaryMessageBufferSize(size);
		// factory.getPolicy().setMaxBinaryMessageSize(size);
		// factory.getPolicy().setMaxTextMessageBufferSize(size);
		// factory.getPolicy().setMaxTextMessageSize(size);

		factory.setCreator(new QWSCreator());
	}

	public static ServletContextHandler createHandler() {
		ServletContextHandler handler = new ServletContextHandler(
				ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
		handler.setContextPath("/");
		ServletHolder sh=new ServletHolder();
		sh.setServlet(new QWSMessagingServlet());
		handler.addServlet(sh, "/*");
		return handler;
	}
}
