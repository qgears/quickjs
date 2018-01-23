package hu.qgears.quickjs.qpage;

import java.io.IOException;

public class QDiv extends QComponent
{
	public QDiv(QPage page, String identifier) {
		super(page, identifier);
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

	public void handle(HtmlTemplate parent, IInMemoryPost post) throws IOException {
	}

	@Override
	public void doInit() {
		setParent(page.getCurrentTemplate());
		write("\tnew QLabel(page, \"");
		writeObject(id);
		write("\");\n");
		setParent(null);
	}

}
