package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;

public class QDateEditor extends QComponent
{
	public final QProperty<String> text=new QProperty<>();
	public final UtilEvent<String> enterPressed=new UtilEvent<>();
	public QDateEditor(IQContainer container, String identifier) {
		super(container, identifier);
		init();
	}
	public QDateEditor(IQContainer container) {
		super(container);
		init();
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

	public void handle(JSONObject post) throws IOException {
		String ntext=post.optString("text", null);
		if(ntext!=null)
		{
			text.setPropertyFromClient(ntext);
		}
		String exter=post.optString("enter", null);
		if(exter!=null)
		{
			enterPressed.eventHappened(exter);
		}
	}

	@Override
	public void doInitJSObject() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tnew QDateEditor(page, \"");
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
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
