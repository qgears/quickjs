package quickjs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

public class HelloWorld {
	static byte[] staticreply="Hello!".getBytes(StandardCharsets.UTF_8);
	static public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
		String path=request.getPathInfo();
		switch(path)
		{
		case "/performance":
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(staticreply);
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			break;
		case "/query":
			new Query().serve(target, baseRequest, request, response);
		}
		new IndexHtml().serve(target, baseRequest, request, response);
	}
}
