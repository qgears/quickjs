package hu.qgears.quickjs.qpage.jetty.websocket;

import org.eclipse.jetty.server.handler.AbstractHandler;

public class QWSMessagingServlet {
	private QWSMessagingServlet()
	{
	}
	public static AbstractHandler createHandler() {
		WebSocketSimple wss=new WebSocketSimple(new QWSCreator());
		wss.setMaxSize(65536);
		wss.setIdleTimeout(QWSMessagingClass.pingPeriodMillis*2);
		return wss.createHandler();
	}
}
