package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEventListener;

public class QRange extends QComponent
{
	public final QProperty<Integer> value=new QProperty<>(0);
	public QRange(IQContainer container, String identifier) {
		super(container, identifier);
		init();
	}
	public QRange(IQContainer container) {
		super(container);
		init();
	}
	protected void serverCheckedChanged(final int value) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].initValue(");
			writeObject(""+value);
			write(");\n");
		}
	}
	@Override
	public void generateHtmlObject() {
		write("<input id=\"");
		writeObject(id);
		write("\" type=\"range\">\n");
	}
	@Override
	public void handle(JSONObject post) throws IOException {
		int value=post.getInt("value");
		this.value.setPropertyFromClient(value);
	}

	@Override
	public void doInitJSObject() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tnew QRange(page, \"");
			writeObject(id);
			write("\").initValue(");
			writeJSValue(""+value.getProperty());
			write(");\n");
			value.serverChangedEvent.addListener(new UtilEventListener<Integer>() {
				@Override
				public void eventHappened(Integer msg) {
					serverCheckedChanged(msg);
				}
			});
		}
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
