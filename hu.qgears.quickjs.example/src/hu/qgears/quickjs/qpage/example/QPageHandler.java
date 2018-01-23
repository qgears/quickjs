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
import hu.qgears.quickjs.utils.AbstractQPage;
import hu.qgears.quickjs.utils.HttpSessionQPageManager;
import hu.qgears.quickjs.utils.InMemoryPost;

/**
 * Jetty compatible http query handler that includes an {@link AbstractQPage}.
 * 
 * This is the only Jetty specific part of the example implementation.
 * This logic must be reimplemented to include QPage applications within a servlet for example.
 */
public class QPageHandler {
	private IQPageFactory pageFactory;
	public QPageHandler(IQPageFactory pageFactory) {
		this.pageFactory=pageFactory;
	}
	public QPageHandler(Class<? extends AbstractQPage> pageClass) {
		this.pageFactory=req->pageClass.newInstance();
	}

	public void handle(String target, final Request baseRequest, HttpServletRequest request, HttpServletResponse response, Object userData) throws IOException {
 		HttpSession sess=baseRequest.getSession();
		final QPageManager qpm=HttpSessionQPageManager.getManager(sess);
		String id=baseRequest.getParameter(QPage.class.getSimpleName());
		try {
			response.setContentType("text/html; charset=utf-8");
			final Writer wr=new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
			switch(baseRequest.getMethod())
			{
			case "GET":
				if(id==null)
				{
					// handle initial get
					new HtmlTemplate(wr){
						public void generate() throws Exception {
							QPage newPage=new QPage(qpm);
							AbstractQPage inst=pageFactory.createPage(userData);
							inst.initApplication(this, newPage);
						}
					}.generate();
				}else
				{
					System.err.println("Page exists query invalid!");
				}
				break;
			default:
				final QPage page=qpm.getPage(id);
				if(page!=null)
				{
					// handle posts
					new HtmlTemplate(wr){
						public void generate() throws Exception {
							InMemoryPost post=new InMemoryPost(baseRequest);
							page.handle(this, post);
							return;
						}
					}.generate();
				}
				break;
			}
			baseRequest.setHandled(true);
			wr.close();
		}catch(Exception e)
		{
			throw new IOException("Processing page: "+target+" "+baseRequest.getMethod(), e);
		}
	}

}
