package hu.qgears.quickjs.utils;

import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Request;

import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.qpage.example.QPageHandler;

/**
 * Base class of a QPage based Web application.
 * This class does not depend on any web server implementation and can be adapted to any 
 * Java based web server.
 */
abstract public class AbstractQPage extends HtmlTemplate {
	protected Object userData;
	public AbstractQPage() {
		super((Writer)null);
	}

	protected QPage page;
	/**
	 * Initialize QPage object and generate initial HTML content.
	 * @param parent
	 */
	public void initApplication(HtmlTemplate parent, QPage newPage)
	{
		setWriter(parent.getWriter());
		page=newPage;
		initQPage(page);
		generateHtmlContent();
		afterPageInitialized();
	}

	/**
	 * Called after all initialization was done.
	 * Default implementation does nothing.
	 * Subclasses may override.
	 */
	protected void afterPageInitialized()
	{
	}
	/**
	 * Initialize the QPage object by adding all the initial QComponents to it. Also add business logic as listeners.
	 * This is called first before writeBody();
	 * @return
	 */
	abstract protected void initQPage(QPage page);

	protected void generateHtmlContent() {
		write("<!DOCTYPE html>\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
		writeFaviconHeaders();
		writeHeaders();
		page.writeHeaders(this);
		write("</head>\n<body>\n");
		setupWebSocketArguments(false);
		page.setCurrentTemplate(this);
		writeBody();
		page.setCurrentTemplate(null);
		page.generateInitialization(this);
		bodyAfterQPageInitialization();
		write("</body>\n</html>\n");
	}

	/**
	 * Subclasses may override. Default implementation disables favicons.
	 */
	protected void writeFaviconHeaders() {
		write("<link rel=\"icon\" href=\"data:;base64,=\">\n");
	}
	/**
	 * Subclasses can add additional HTML or script to the main page after the QPage class was initialized.
	 * We are still within the body so HTML elements are valid.
	 */
	protected void bodyAfterQPageInitialization() {
		
	}
	/**
	 * Write headers into the head section of the HTML page.
	 */
	protected void writeHeaders() {
	}

	/**
	 * Generate the body of the page.
	 */
	protected abstract void writeBody();

	public void setUserData(Object req) {
		userData=req;
	}
	/**
	 * Subclasses can override this method to handle initial query request object.
	 * @param request
	 */
	public void setRequest(Request baseRequest, HttpServletRequest request) {
	}
	/**
	 * Subclasses can override this method to create additional websocket handlers.
	 * This is called once when initializing the handler and called not on a query but on initialization
	 * on a dummy instance of the page.
	 * @param qPageHandler
	 */
	public void configureWebsocketHandlers(QPageHandler qPageHandler)
	{
	}
}
