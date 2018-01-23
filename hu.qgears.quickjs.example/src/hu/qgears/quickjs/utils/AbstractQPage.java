package hu.qgears.quickjs.utils;

import java.io.Writer;

import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.qpage.QPage;

/**
 * Base class of a QPage based Web application.
 * This class does not depend on any web server implementation and can be adapted to any 
 * Java based web server.
 */
abstract public class AbstractQPage extends HtmlTemplate {
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
	}

	/**
	 * Initialize the QPage object by adding all the initial QComponents to it. Also add business logic as listeners.
	 * This is called first before writeBody();
	 * @return
	 */
	abstract protected void initQPage(QPage page);

	protected void generateHtmlContent() {
		write("<!DOCTYPE html>\n<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
		page.writeHeaders(this);
		writeHeaders();
		write("</head>\n<body>\n");
		page.setCurrentTemplate(this);
		writeBody();
		page.setCurrentTemplate(null);
		page.generateInitialization(this);
		write("</body>\n</html>\n");
	}

	protected void writeHeaders() {
	}

	/**
	 * Generate the 
	 */
	protected abstract void writeBody();
}
