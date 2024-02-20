package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

public class QDiv extends QComponent
{
	public QDiv(IQContainer container, String identifier) {
		super(container, identifier);
	}
	public QDiv(IQContainer container) {
		super(container);
	}
	@Override
	public void generateHtmlObject() {
		write("<div id=\"");
		writeObject(id);
		write("\"></div>\n");
	}
	public void handle(HtmlTemplate parent, JSONObject post) throws IOException {
	}
	@Override
	public void doInitJSObject() {
		write("\tnew QDiv(page, \"");
		writeObject(id);
		write("\");\n");
	}
}
