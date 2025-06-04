package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.commons.ConnectStreams;
import hu.qgears.commons.UtilFile;
import hu.qgears.quickjs.serverside.QPageTypesRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
		response.setHeader("Content-Encoding", "br");
		UtilJetty.setResponseCacheable(response);
		byte[] data=UtilFile.loadFile(QPageTypesRegistry.getInstance().getResource(resname));
		ProcessBuilder pb=new ProcessBuilder("/usr/bin/brotli", "-c", "-");
		pb.redirectError(Redirect.INHERIT);
		System.out.println("Brotli start: "+target);
		Process p=pb.start();
		System.out.println("Brotli started: "+target);
		new Thread() {
			public void run() {
				try {
					p.getOutputStream().write(data);
					p.getOutputStream().close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
		ConnectStreams.doStream(p.getInputStream(), response.getOutputStream());
		try {
			p.waitFor(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Brotli done");
		// response.getOutputStream().write(data);
	}
}
