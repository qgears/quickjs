package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.UtilEventListener;

public class QCheckbox extends QComponent
{
	public final QProperty<Boolean> checked=new QProperty<>(false);
	public QCheckbox(IQContainer container, String identifier) {
		super(container, identifier);
	}
	public QCheckbox(IQContainer container) {
		super(container);
	}
	protected void serverCheckedChanged(final boolean checked) {
		try(ResetOutputObject roo=setParent(page.getCurrentTemplate()))
		{
			write("page.components['");
			writeJSValue(id);
			write("'].initValue(");
			writeObject(""+checked);
			write(");\n");
		}
	}
	@Override
	public void generateHtmlObject() {
		write("<input id=\"");
		writeObject(id);
		write("\" type=\"checkbox\">\n");
	}

	public void handle(HtmlTemplate parent, JSONObject post) throws IOException {
		boolean checked=post.getBoolean("checked");
		this.checked.setPropertyFromClient(checked);
	}

	@Override
	public void doInitJSObject() {
		setParent(page.getCurrentTemplate());
		write("\tnew QCheckbox(page, \"");
		writeObject(id);
		write("\").initValue(");
		writeJSValue(""+checked.getProperty());
		write(");\n");
		setParent(null);
		checked.serverChangedEvent.addListener(new UtilEventListener<Boolean>() {
			@Override
			public void eventHappened(Boolean msg) {
				serverCheckedChanged(msg);
			}
		});
	}
}
