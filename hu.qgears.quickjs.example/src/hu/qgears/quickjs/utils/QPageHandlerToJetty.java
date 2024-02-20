package hu.qgears.quickjs.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerCollection;

import hu.qgears.quickjs.qpage.example.QPageHandler;

/**
 * Use {@link QPageHandler} that is already a Jetty handler.
 */
@Deprecated
public class QPageHandlerToJetty extends HandlerCollection {
	QPageHandler handler;
	Object userData;
	public QPageHandlerToJetty(QPageHandler handler, Object userData) {
		super();
		this.handler = handler;
		this.userData=userData;
		addHandler(handler);
	}


	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		QPageHandler.setUserParameter(request, userData);
		handler.handle(target, baseRequest, request, response);
	}
}
