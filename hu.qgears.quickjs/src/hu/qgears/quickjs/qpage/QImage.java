package hu.qgears.quickjs.qpage;

import java.io.IOException;

import hu.qgears.commons.UtilEventListener;

public class QImage extends QComponent
{
	public final QProperty<String> src=new QProperty<>();
	public QImage(QPage page, String identifier) {
		super(page, identifier);
	}

	public void generateExampleHtmlObject(HtmlTemplate parent) {
		new HtmlTemplate(parent){

			public void generate() {
				write("<img id=\"");
				writeObject(id);
				write("\"></img>\n");
			}
			
		}.generate();		
	}

	public void handle(HtmlTemplate parent, IInMemoryPost post) throws IOException {
	}

	@Override
	public void doInit() {
		setParent(page.getCurrentTemplate());
		write("\tnew QImage(page, \"");
		writeObject(id);
		write("\").initSrc(\"");
		writeJSValue(src.getProperty());
		write("\");\n");
		setParent(null);
		src.serverChangedEvent.addListener(new UtilEventListener<String>() {
			@Override
			public void eventHappened(String msg) {
				srcChanged(msg);
			}
		});
	}
	protected void srcChanged(final String msg) {
		if(page.inited)
		{
			new ChangeTemplate(page.getCurrentTemplate()){
				public void generate() {
					write("page.components['");
					writeJSValue(id);
					write("'].initSrc(\"");
					writeJSValue(msg);
					write("\");\n");
				}
			}.generate();
		}
	}
}
