package hu.qgears.quickjs.qpage;

/**
 * Base class of a QPage "code behind" that drives a single HTML page.
 */
abstract public class AbstractQPage extends HtmlTemplate {
	abstract protected void startPage();
}
