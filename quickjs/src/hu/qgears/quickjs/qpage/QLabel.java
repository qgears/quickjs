package hu.qgears.quickjs.qpage;

import java.io.IOException;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.quickjs.utils.InMemoryPost;

public class QLabel extends QComponent
{
	public final QProperty<String> innerhtml=new QProperty<>();
	public QLabel(QPage page, String identifier) {
		super(page, identifier);
		innerhtml.serverChangedEvent.addListener(new UtilEventListener<String>() {
			
			@Override
			public void eventHappened(String msg) {
				textChanged(msg);
			}
		});
	}

	public static void generateHeader(HtmlTemplate parent)
	{
		new HtmlTemplate(parent){

			public void generate() {
				write("<script language=\"javascript\" type=\"text/javascript\">\nclass QLabel extends QComponent\n{\n\taddDomListeners()\n\t{\n\t}\n\tinitValue(text)\n\t{\n\t\tthis.dom.innerHTML=text;\n\t}\n}\n</script>\n");
			}
			
		}.generate();
	}

	public void generateExampleHtmlObject(HtmlTemplate parent) {
		new HtmlTemplate(parent){

			public void generate() {
				write("<div id=\"");
				writeObject(id);
				write("\"></div>\n");
			}
			
		}.generate();		
	}

	public void handle(HtmlTemplate parent, InMemoryPost post) throws IOException {
	}

	@Override
	public void init(HtmlTemplate parent) {
		new HtmlTemplate(parent)
		{

			public void generate() {
				write("\tnew QLabel(page, \"");
				writeObject(id);
				write("\").initValue(\"");
				writeJSValue(innerhtml.getProperty());
				write("\");\n");
			}
			
		}.generate();
	}
	protected void textChanged(final String msg) {
		if(page.inited)
		{
			new ChangeTemplate(page.getCurrentTemplate()){
				public void generate() {
					write("page.components['");
					writeJSValue(id);
					write("'].initValue(\"");
					writeJSValue(msg);
					write("\");\n");
				}
			}.generate();
		}
	}
}
