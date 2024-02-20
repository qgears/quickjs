package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;

public class QDateEditor extends QComponent
{
	public final QProperty<String> text=new QProperty<>();
	public final UtilEvent<String> enterPressed=new UtilEvent<>();
	public QDateEditor(IQContainer container, String identifier) {
		super(container, identifier);
	}
	public QDateEditor(IQContainer container) {
		super(container);
	}
	protected void serverTextChanged(final String msg) {
		try(ResetOutputObject roo=setParent(page.getCurrentTemplate()))
		{
			write("page.components['");
			writeJSValue(id);
			write("'].initValue(\"");
			writeJSValue(msg);
			write("\");\n");
		}
	}
	@Override
	public void generateHtmlObject() {
		write("<input type=\"date\" id=\"");
		writeObject(id);
		write("\"></input>\n");
	}

	public void handle(HtmlTemplate parent, JSONObject post) throws IOException {
		String ntext=JSONHelper.getStringSafe(post,"text");
		if(ntext!=null)
		{
			text.setPropertyFromClient(ntext);
		}
		String exter=JSONHelper.getStringSafe(post, "enter");
		if(exter!=null)
		{
			enterPressed.eventHappened(exter);
		}
	}

	@Override
	public void doInitJSObject() {
		setParent(page.getCurrentTemplate());
		write("\tnew QDateEditor(page, \"");
		writeObject(id);
		write("\").initValue(\"");
		writeJSValue(text.getProperty());
		write("\");\n");
		setParent(null);
		text.serverChangedEvent.addListener(new UtilEventListener<String>() {
			@Override
			public void eventHappened(String msg) {
				serverTextChanged(msg);
			}
		});
	}
}
