package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;

public class QTextEditor extends QComponent
{
	public final QProperty<String> text=new QProperty<>();
	public final UtilEvent<String> enterPressed=new UtilEvent<>();
	public QTextEditor(IQContainer container, String identifier) {
		super(container, identifier);
		init();
	}
	public QTextEditor() {
		super();
		init();
	}
	public QTextEditor(IQContainer container) {
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
//	@Override
//	public void generateHtmlObject() {
//		write("<textarea id=\"");
//		writeObject(id);
//		write("\" rows=\"4\" cols=\"50\"></textarea>\n");
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
			write("\tnew QTextEditor(page, \"");
			writeObject(id);
			write("\")");
			initialDisabledState();
			write(".initValue(\"");
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
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
