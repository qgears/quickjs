package quickjs;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class HelloWorld extends AbstractHandler {
	public static void main(String[] args) throws Exception {
		InetSocketAddress sa=new InetSocketAddress("127.0.0.1", 8888);
		Server s=new Server(sa);
		HelloWorld dh=new HelloWorld();
		s.setHandler(dh);
		s.start();
		s.join();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
		String path=request.getPathInfo();
		switch(path)
		{
		case "/query":
			new Query().serve(target, baseRequest, request, response);
		}
		new IndexHtml().serve(target, baseRequest, request, response);
	}
}
