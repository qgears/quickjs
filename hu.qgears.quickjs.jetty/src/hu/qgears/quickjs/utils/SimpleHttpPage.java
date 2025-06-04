package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.quickjs.qpage.HtmlTemplate;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SimpleHttpPage extends HtmlTemplate implements Cloneable
{
	Logger log=LoggerFactory.getLogger(getClass());
	public AbstractHandler createHandler()
	{
		return new AbstractHandler() {
			
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				try {
					SimpleHttpPage p=(SimpleHttpPage)SimpleHttpPage.this.clone();
					p.handle(target, baseRequest, request, response);
				} catch (CloneNotSupportedException e) {
					// This never happens
					log.error("Creating page by cloning the master instance.", e);
				}
			}
		};
	}
	protected Request baseRequest;
	protected HttpServletResponse response;
	private String mimeType="text/html";
	protected void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		this.baseRequest=baseRequest;
		this.response=response;
		response.setContentType(mimeType+"; charset=utf-8");
		
		try(final Writer wr=new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))
		{
			setWriter(wr);
			handlePage();
			baseRequest.setHandled(true);
		}
	}

	protected void handlePage() {
		write("<!DOCTYPE html>\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
		writeFaviconHeaders();
		writeHeaders();
		write("</head>\n<body id=\"bodyId\">\n");
		writeBody();
		write("</body>\n</html>\n");
	}
	/**
	 * Subclasses may override. Default implementation disables favicons.
	 */
	protected void writeFaviconHeaders() {
		write("<link rel=\"icon\" href=\"data:;base64,=\">\n");
	}

	/**
	 * Subclasses should override.
	 */
	protected void writeBody() {
	}

	/**
	 * Subclasses should override.
	 */
	protected void writeHeaders() {
	}
	public SimpleHttpPage setMimeType(String mimeType) {
		this.mimeType = mimeType;
		return this;
	}
}
