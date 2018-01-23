package hu.qgears.quickjs.qpage;

public class QSelectCombo extends QSelect {

	public QSelectCombo(QPage page0, String id) {
		super(page0, id);
	}
	
	@Override
	public void generateExampleHtmlObject(HtmlTemplate parent) {
		setWriter(parent.getWriter());
		write("<select id=\"");
		writeJSValue(id);
		write("\" style=\"width: 250px;\" size=10></select>\t\n");
		setWriter(null);
	}
}
