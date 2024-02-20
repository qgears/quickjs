package hu.qgears.quickjs.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.commons.UtilFile;
import hu.qgears.quickjs.qpage.QPageTypesRegistry;

/**
 * HTTP Handlder that serves the JS files of the QuickJS framework.
 */
public class QuickJsHandler extends AbstractHandler
{
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest arg2, HttpServletResponse response)
			throws IOException, ServletException {
		String resname=target.substring("/".length());
		baseRequest.setHandled(true);
		response.setContentType("text/javascript");
		byte[] data=UtilFile.loadFile(QPageTypesRegistry.getInstance().getResource(resname));
		response.getOutputStream().write(data);
	}
}
