package hu.qgears.quickjs.qpage;

import java.io.IOException;

import hu.qgears.commons.UtilEvent;

public class QButton extends QComponent
{
	public final UtilEvent<QButton> clicked=new UtilEvent<>();
	public QButton(QPage page, String identifier) {
		super(page, identifier);
	}
	
	static public void generateHeader(HtmlTemplate parent)
	{
		new HtmlTemplate(parent){

			public void generate() {
				write("<script language=\"javascript\" type=\"text/javascript\">\n\nclass QButton extends QComponent\n{\n\taddDomListeners()\n\t{\n\t\tthis.dom.addEventListener(\"click\", this.oninput.bind(this), false);\n\t}\n\toninput()\n\t{\n\t\tvar fd=this.page.createFormData(this);\n\t\tthis.page.send(fd);\n\t}\n}\n</script>\n");
			}
			
		}.generate();
	}

	public void generateExampleHtmlObject(HtmlTemplate parent) {
		new HtmlTemplate(parent){
			public void generate() {
				write("<button id=\"");
				writeObject(id);
				write("\">BUTTON</button>\n");
			}
		}.generate();		
	}

	public void handle(HtmlTemplate parent, IInMemoryPost post) throws IOException {
		clicked.eventHappened(this);
	}

	@Override
	public void init(HtmlTemplate parent) {
		new HtmlTemplate(parent)
		{

			public void generate() {
				write("\tnew QButton(page, \"");
				writeObject(id);
				write("\");\n");
			}
			
		}.generate();
	}
}
