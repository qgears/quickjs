package hu.qgears.quickjs.qpage.jetty.websocket;

import java.io.IOException;
import java.time.Duration;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class WebSocketSimple extends JettyWebSocketServlet {
	private static final long serialVersionUID = 1L;

	private JettyWebSocketCreator wsCreator;
	private Integer maxSize;
	private Long idleTimeout=QWSMessagingClass.pingPeriodMillis*2;
	private WebSocketCreator wsc;
	
	/**
	 * @param wsc The creator has to return a WebSocketAdapter
	 */
	public WebSocketSimple(WebSocketCreator wsc) {
		super();
		this.wsc=wsc;
		this.wsCreator=new JettyWebSocketCreator() {
			@Override
			public Object createWebSocket(JettyServerUpgradeRequest req, JettyServerUpgradeResponse resp) {
				WebSocketCreator wscAsAttribute=(WebSocketCreator)req.getHttpServletRequest().getAttribute(WebSocketCreator.class.getSimpleName());
				WebSocketCreationContext c=(WebSocketCreationContext)req.getHttpServletRequest().getAttribute(WebSocketCreationContext.class.getSimpleName());
				c.req=req;
				c.resp=resp;
				return wscAsAttribute.createWebSocket(c);
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
		JettyWebSocketServletContainerInitializer.configure(handler, (servletContext, c)->{
			System.out.println("Configurer: "+servletContext+" "+c);
		});
		HandlerCollection ret=new HandlerCollection() {
			@Override
			public void handle(String arg0, Request arg1, HttpServletRequest arg2, HttpServletResponse arg3)
					throws IOException, ServletException {
				Object prev=arg2.getAttribute(WebSocketCreator.class.getSimpleName());
				try
				{
					arg2.setAttribute(WebSocketCreator.class.getSimpleName(), wsc);
					WebSocketCreationContext c=new WebSocketCreationContext();
					c.r=arg1;
					arg2.setAttribute(WebSocketCreationContext.class.getSimpleName(), c); 
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
	public void configure(JettyWebSocketServletFactory factory) {
		if(idleTimeout!=null)
		{
			factory.setIdleTimeout(Duration.ofMillis(idleTimeout));
		}
		if(maxSize!=null)
		{
			factory.setMaxBinaryMessageSize(maxSize);
			factory.setMaxTextMessageSize(maxSize);
		}
		factory.setCreator(wsCreator);
	}
	public WebSocketSimple setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
		return this;
	}
	public void setIdleTimeout(Long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}
}
