package hu.qgears.quickjs.qpage;

/**
 * Base class of a QPage "code behind" that drives a single HTML page.
 * In case of an SPA "navigation" the life cycle ends with the navigation and the new page starts.
 */
abstract public class AbstractQPage extends HtmlTemplate {
	/** The context object is always accessible and set by the framework before the first method call to user code. */
	protected IQPageContaierContext context;
	/** The page object is always accessible and set by the framework before the first method call to user code. */
	protected QPage page;
	/**
	 * SERVER/CLIENT
	 * In this phase the meta data of the page is created:
	 * title, favicon and other parts of the <head> part of the HTML page.
	 * 
	 *  * In case of initial SERVER execution the program will put these values into the initial
	 * HTML reply.
	 *  * In case of CLIENT execution or page replace execution the values are dynamically updated
	 */
	abstract protected void initPage();
	/**
	 * SERVER/CLIENT
	 * In this phase the initial HTML content of the page is generated.
	 * What is generated here is the HTML content of the body tag.
	 */
	abstract protected void startPage();
}
