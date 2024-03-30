package hu.qgears.quickjs.qpage;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.NoExceptionAutoClosable;

/** Base class of a QPage "code behind" that drives a single HTML page.
 * In case of an SPA "navigation" the life cycle ends with the navigation and the new page starts.
 */
abstract public class AbstractQPage extends HtmlTemplate {
	/** The context object is always accessible and set by the framework before the first method call to user code. */
	protected IQPageContaierContext pageContainerContext;
	/** The page object is always accessible and set by the framework before the first method call to user code. */
	protected QPage page;
	private List<String> cssUrl=new ArrayList<>();
	private List<String> jsPreloadUrl=new ArrayList<>();
	/** Called by the framework to set up the context of the page. */
	final public AbstractQPage setPageContext(IQPageContaierContext context)
	{
		this.page=context.getPage();
		this.pageContainerContext=context;
		return this;
	}
	final public NoExceptionAutoClosable setInitialHtmlOutput(HtmlTemplate htmlTemplate)
	{
		return setParent(htmlTemplate);
	}
	/** SERVER/CLIENT
	 * In this phase the meta data of the page is created:
	 * title, favicon and other parts of the <head> part of the HTML page.
	 * 
	 * In the lifecycle of an SPA the first query is executed on the server.
	 * Intra-SPA navigation executes this on the client.
	 * 
	 *  * In case of initial SERVER execution the program will put these values into the initial
	 * HTML reply.
	 *  * In case of CLIENT execution or page replace execution the values are dynamically updated
	 */
	abstract public void initPage();
	/**
	 * Create the whole HTML content of the page.
	 * Called after initPage().
	 * When called the template that writes the server initial reply is already activated by setInitialHtmlOutput() call.
	 * Calls createBody() internally.
	 * Not intended to be overridden.
	 * In case of SPA application this is only called for the first queried page.
	 * Not intended to be run on the client side because the client is only active when this
	 * method was already executed.
	 */
	public void initialCreateHtml()
	{
		write("<!DOCTYPE html>\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
		initialWriteFaviconHeaders();
		initialWriteHeaders();
		page.getParent().writeHeaders(this);
		write("</head>\n<body>\n");
		setupWebSocketArguments(false);
		createBody();
		page.getParent().generateInitialization(this);
		write("</body>\n</html>\n");
	}
	/**Write additional headers into the head section of the HTML page.
	 * Called after favicon but before QPage related JS initialization.
	 * Default implementation does nothing, can be overridden to add functionality.
	 */
	protected void initialWriteHeaders()
	{
		for (String s: jsPreloadUrl)
		{
			write("<link rel=\"preload\" href=\"");
			writeJSValue(pageContainerContext.getResourcePathSync(s));
			write("\" as=\"script\"/>\n");
			
		}
		for (String css: cssUrl)
		{
			write("<link rel=\"stylesheet\" href=\"");
			writeJSValue(pageContainerContext.getResourcePathSync(css));
			write("\" type=\"text/css\"/>\n");
		}
	}
	/**
	 * Default implementation disables favicon.
	 */
	protected void initialWriteFaviconHeaders()
	{
		write("<link rel=\"icon\" href=\"data:;base64,=\">\n");
	}
	/**
	 * SERVER/CLIENT
	 * In this phase the initial HTML content of the page is generated.
	 * What is generated here is the HTML content of the body tag.
	 */
	abstract public void createBody();
	protected void headCss(String url) {
		cssUrl.add(url);
	}
	protected void headJsPreload(String url) {
		jsPreloadUrl.add(url);
	}
}
