package hu.qgears.quickjs.qpage.example;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Request;

import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.qpage.QPageManager;
import hu.qgears.quickjs.utils.InMemoryPost;

/**
 * Jetty compatible http query handler that includes an {@link AbstractQPage}.
 * 
 * This is the only Jetty specific part of the example implementation.
 * This logic must be reimplemented to include QPage applications within a servlet for example.
 */
public class QPageHandler {
	private Class<? extends AbstractQPage> qPageClass;
	public QPageHandler(Class<? extends AbstractQPage> qPageClass) {
		this.qPageClass=qPageClass;
	}

	public void handle(String target, final Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
 		HttpSession sess=baseRequest.getSession();
		final QPageManager qpm=QPageManager.getManager(sess);
		final QPage page0=qpm.getPage(baseRequest);
		try {
			baseRequest.setHandled(true);
			response.setContentType("text/html; charset=utf-8");
			final Writer wr=new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
			switch(baseRequest.getMethod())
			{
			case "GET":
				// handle initial get
				new HtmlTemplate(wr){
					public void generate() throws Exception {
						QPage newPage=new QPage(qpm);
						AbstractQPage inst=qPageClass.newInstance();
						inst.initApplication(this, newPage);
					}
				}.generate();
				break;
			default:
				// handle posts
				new HtmlTemplate(wr){
					public void generate() throws Exception {
						InMemoryPost post=new InMemoryPost(baseRequest);
						page0.handle(this, post);
						return;
					}
				}.generate();
				break;
			}
			wr.close();
		}catch(Exception e)
		{
			throw new IOException(e);
		}
	}

}
