package hu.qgears.quickjs.qpage;

import java.io.IOException;
import java.io.StringWriter;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEventListener;

public class QLabel extends QComponent
{
	public final QProperty<String> innerhtml=new QProperty<>();
	public QLabel(IQContainer container, String identifier) {
		super(container, identifier);
		init();
	}
	public QLabel(String identifier) {
		super(identifier);
		init();
	}
	public QLabel(IQContainer container) {
		super(container);
		init();
	}
	public QLabel() {
		init();
	}
//	@Override
//	public void generateHtmlObject() {
//		write("<div id=\"");
//		writeObject(id);
//		write("\"></div>\n");
//	}

	public void handle(JSONObject post) throws IOException {
	}

	@Override
	public void doInitJSObject() {
		write("\tnew QLabel(page, \"");
		writeObject(id);
		write("\").setInnerHtml(\"");
		writeJSValue(innerhtml.getProperty());
		write("\");\n");
		innerhtml.serverChangedEvent.addListener(new UtilEventListener<String>() {
			@Override
			public void eventHappened(String msg) {
				textChanged(msg);
			}
		});
	}
	protected void textChanged(final String msg) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].setInnerHtml(\"");
			writeJSValue(msg);
			write("\");\n");
		}
	}
	public void setPreformatted(String content)
	{
		StringWriter sw=new StringWriter();
		try(ResetOutputObject roo=setParent(new HtmlTemplate(sw)))
		{
			write("<pre>");
			writeHtml(content);
			write("</pre>");
		}
		innerhtml.setPropertyFromServer(sw.toString());
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
