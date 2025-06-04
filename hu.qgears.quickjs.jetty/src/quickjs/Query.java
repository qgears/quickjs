package quickjs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import org.eclipse.jetty.server.Request;

import hu.qgears.quickjs.qpage.HtmlTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Query extends HtmlTemplate
{
	public void serve(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		Writer wr=new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
		setWriter(wr);
		generateResponse(request);
		wr.close();
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	}

	private void generateResponse(HttpServletRequest request) throws IOException {
		Enumeration<String> names=request.getParameterNames();
		while(names.hasMoreElements())
		{
			String name=names.nextElement();
			writeObject(name);
			write(": ");
			writeObject(request.getParameter(name));
			write("<br/>\n");
		}
	}
}
