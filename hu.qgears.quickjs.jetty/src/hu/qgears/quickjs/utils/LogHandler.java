package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Receive a JSON object and send each object to the error log.
 */
public class LogHandler extends AbstractHandler {
	Logger log=LoggerFactory.getLogger("JavaScript");
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String ct=request.getHeader("Content-Type");
		if("application/json;charset=UTF-8".equals(ct))
		{
			try(ServletInputStream sis=baseRequest.getInputStream())
			{
				StringBuilder sb=new StringBuilder();
				Reader r=new InputStreamReader(sis, StandardCharsets.UTF_8);
				int v;
				while((v=r.read())>=0)
				{
					sb.append((char)v);
				}
				JSONArray arr=new JSONArray(sb.toString());
				for(int i=0;i<arr.length();++i)
				{
					log.error(""+arr.get(i));
				}
			}
			response.setStatus(HttpServletResponse.SC_OK);
		}else
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Content-Type header: "+ct);
		}
		baseRequest.setHandled(true);
	}
}
