package hu.qgears.quickjs.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.quickjs.qpage.example.QPageHandler;

public class QPageHandlerToJetty extends AbstractHandler {
	QPageHandler handler;
	Object userData;

	public QPageHandlerToJetty(QPageHandler handler, Object userData) {
		super();
		this.handler = handler;
		this.userData=userData;
	}


	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		handler.handle(target, baseRequest, request, response, userData);
	}

}
