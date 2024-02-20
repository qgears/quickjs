package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.UtilEventListener;

public class QRange extends QComponent
{
	public final QProperty<Integer> value=new QProperty<>(0);
	public QRange(IQContainer container, String identifier) {
		super(container, identifier);
	}
	public QRange(IQContainer container) {
		super(container);
	}
	protected void serverCheckedChanged(final int value) {
		try(ResetOutputObject roo=setParent(page.getCurrentTemplate()))
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

	public void handle(HtmlTemplate parent, JSONObject post) throws IOException {
		int value=post.getInt("value");
		this.value.setPropertyFromClient(value);
	}

	@Override
	public void doInitJSObject() {
		setParent(page.getCurrentTemplate());
		write("\tnew QRange(page, \"");
		writeObject(id);
		write("\").initValue(");
		writeJSValue(""+value.getProperty());
		write(");\n");
		setParent(null);
		value.serverChangedEvent.addListener(new UtilEventListener<Integer>() {
			@Override
			public void eventHappened(Integer msg) {
				serverCheckedChanged(msg);
			}
		});
	}
}
