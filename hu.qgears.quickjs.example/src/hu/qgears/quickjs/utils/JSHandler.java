package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.commons.UtilFile;

abstract public class JSHandler extends AbstractHandler
{

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String pathinfo=baseRequest.getPathInfo();
		if(handlesJs(pathinfo))
		{
			response.setContentType(getMimeType());
			try(OutputStream os=response.getOutputStream())
			{
				writeTo(os, pathinfo);
				response.setStatus(HttpServletResponse.SC_OK);
			}catch(Exception e)
			{
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				e.printStackTrace();
			}
			baseRequest.setHandled(true);
			return;
		}
	}

	protected String getMimeType() {
		return "application/x-javascript";
	}

	protected boolean handlesJs(String pathinfo) {
		return findResource(pathinfo)!=null;
	}

	protected void writeTo(OutputStream os, String pathinfo) throws IOException {
		os.write(UtilFile.loadFile(findResource(pathinfo)));
	}

	abstract protected URL findResource(String pathinfo);

}
