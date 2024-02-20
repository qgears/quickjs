package hu.qgears.quickjs.qpage;

import java.io.StringWriter;

/**
 * Useful class to implement inline HTML templates within a class that is not a
 * HTML template by its own type.
 */
abstract public class QuickHtmlTemplate extends HtmlTemplate
{
	public QuickHtmlTemplate(HtmlTemplate parent)
	{
		super(parent);
	}
	public QuickHtmlTemplate() {
		super(new StringWriter());
	}
	final public String generate()
	{
		try {
			doGenerate();
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return out.toString();
	}
	abstract protected void doGenerate() throws Exception;
}
