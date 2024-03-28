package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;

public class QTimeEditor extends QComponent
{
	public final QProperty<String> text=new QProperty<>();
	public final UtilEvent<String> enterPressed=new UtilEvent<>();
	public QTimeEditor(IQContainer container, String identifier) {
		super(container, identifier);
	}
	public QTimeEditor(IQContainer container) {
		super(container);
	}
	protected void serverTextChanged(final String msg) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].initValue(\"");
			writeJSValue(msg);
			write("\");\n");
		}
	}
//	@Override
//	public void generateHtmlObject() {
//		write("<input type=\"time\" id=\"");
//		writeObject(id);
//		write("\"></input>\n");
//	}

	public void handle(JSONObject post) throws IOException {
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
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tnew QTimeEditor(page, \"");
			writeObject(id);
			write("\").initValue(\"");
			writeJSValue(text.getProperty());
			write("\");\n");
			text.serverChangedEvent.addListener(new UtilEventListener<String>() {
				@Override
				public void eventHappened(String msg) {
					serverTextChanged(msg);
				}
			});
		}
	}
}
