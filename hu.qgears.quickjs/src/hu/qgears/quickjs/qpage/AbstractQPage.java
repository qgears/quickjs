package hu.qgears.quickjs.qpage;

/**
 * Base class of a QPage "code behind" that drives a single HTML page.
 */
abstract public class AbstractQPage extends HtmlTemplate {
	/**
	 * SERVER/CLIENT
	 * In this phase the initial HTML content of the page is generated.
	 * What is generated here is the HTML content of the body tag.
	 */
	abstract protected void startPage();
}
