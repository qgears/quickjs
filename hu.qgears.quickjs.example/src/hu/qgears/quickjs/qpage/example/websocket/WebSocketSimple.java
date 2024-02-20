package hu.qgears.quickjs.qpage.example.websocket;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class WebSocketSimple extends WebSocketServlet {
	private static final long serialVersionUID = 1L;

	private WebSocketCreator wsCreator;
	private Integer maxSize;
	private WebSocketCreator wsc;
	
	/**
	 * @param wsc The creator has to return a WebSocketAdapter
	 */
	public WebSocketSimple(WebSocketCreator wsc) {
		super();
		this.wsc=wsc;
		this.wsCreator=new WebSocketCreator() {
			@Override
			public Object createWebSocket(ServletUpgradeRequest arg0, ServletUpgradeResponse arg1) {
				WebSocketCreator wscAsAttribute=(WebSocketCreator)arg0.getHttpServletRequest().getAttribute(WebSocketCreator.class.getSimpleName());
				return wscAsAttribute.createWebSocket(arg0, arg1);
			}
		};
	}

	public AbstractHandler createHandler()
	{
		ServletContextHandler handler = new ServletContextHandler(
				ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
		handler.setContextPath("/");
		ServletHolder sh=new ServletHolder();
		sh.setServlet(this);
		handler.addServlet(sh, "/*");
		HandlerCollection ret=new HandlerCollection() {
			@Override
			public void handle(String arg0, Request arg1, HttpServletRequest arg2, HttpServletResponse arg3)
					throws IOException, ServletException {
				Object prev=arg2.getAttribute(WebSocketCreator.class.getSimpleName());
				try
				{
					arg2.setAttribute(WebSocketCreator.class.getSimpleName(), wsc);
					super.handle(arg0, arg1, arg2, arg3);
				}finally
				{
					arg2.setAttribute(WebSocketCreator.class.getSimpleName(), prev);
				}
			}
		};
		ret.addHandler(handler);
		return ret;
	}

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.getPolicy().setIdleTimeout(QWSMessagingClass.pingPeriodMillis*2);
		if(maxSize!=null)
		{
			factory.getPolicy().setMaxBinaryMessageBufferSize(maxSize);
			factory.getPolicy().setMaxBinaryMessageSize(maxSize);
			factory.getPolicy().setMaxTextMessageBufferSize(maxSize);
			factory.getPolicy().setMaxTextMessageSize(maxSize);
		}
		factory.setCreator(wsCreator);
	}
	public WebSocketSimple setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
		return this;
	}
}
