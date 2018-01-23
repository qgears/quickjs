package hu.qgears.quickjs.qpage;

import java.io.IOException;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;

public class QTextEditor extends QComponent
{
	public final QProperty<String> text=new QProperty<>();
	public final UtilEvent<String> enterPressed=new UtilEvent<>();
	public QTextEditor(QPage page, String identifier) {
		super(page, identifier);
		text.serverChangedEvent.addListener(new UtilEventListener<String>() {
			@Override
			public void eventHappened(String msg) {
				serverTextChanged(msg);
			}
		});
	}
	
	protected void serverTextChanged(final String msg) {
		if(inited)
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

	public void generateExampleHtmlObject(HtmlTemplate parent) {
		new HtmlTemplate(parent){

			public void generate() {
				write("<textarea id=\"");
				writeObject(id);
				write("\" rows=\"4\" cols=\"50\"></textarea>\n");
			}
			
		}.generate();		
	}

	public void handle(HtmlTemplate parent, IInMemoryPost post) throws IOException {
		String ntext=post.getParameter("text");
		if(ntext!=null)
		{
			text.setPropertyFromClient(ntext);
		}
		String exter=post.getParameter("enter");
		if(exter!=null)
		{
			enterPressed.eventHappened(exter);
		}
	}

	@Override
	public void doInit() {
		setParent(page.getCurrentTemplate());
		write("\tnew QTextEditor(page, \"");
		writeObject(id);
		write("\").initValue(\"");
		writeJSValue(text.getProperty());
		write("\");\n");
		setParent(null);
	}
}
