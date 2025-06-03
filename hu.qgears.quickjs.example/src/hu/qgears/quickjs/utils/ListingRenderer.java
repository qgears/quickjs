package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.server.Request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ListingRenderer extends SimpleHttpPage {
	List<String> names;
	public ListingRenderer(List<String> names) {
		this.names=names;
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		super.handle(target, baseRequest, request, response);
	}
	@Override
	protected void writeBody() {
		write("<a href=\"../\">../</a><br/>\n");
		for(String s: names)
		{
			write("<a href=\"");
			writeHtml(s);
			write("\">");
			writeHtml(s);
			write("</a><br/>\n");
			
		}
	}
}
