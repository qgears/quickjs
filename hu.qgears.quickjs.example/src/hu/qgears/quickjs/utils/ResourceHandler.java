package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.commons.ConnectStreams;

public class ResourceHandler extends AbstractHandler {
	private java.util.function.Supplier<InputStream> contentOpener;
	private String mimeType;
	public ResourceHandler(String mimeType, Supplier<InputStream> contentOpener) {
		super();
		this.mimeType = mimeType;
		this.contentOpener = contentOpener;
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		UtilJetty.setResponseCacheable(response);
		response.setContentType(mimeType);
		try(InputStream is=contentOpener.get())
		{
			ConnectStreams.doStream(is, response.getOutputStream());
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
		}
	}
}
