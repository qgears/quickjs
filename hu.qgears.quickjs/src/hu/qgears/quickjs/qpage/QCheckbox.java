package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEventListener;

public class QCheckbox extends QComponent
{
	public final QProperty<Boolean> checked=new QProperty<>(false);
	public QCheckbox(IQContainer container, String identifier) {
		super(container, identifier);
		init();
	}
	public QCheckbox(IQContainer container) {
		super(container);
		init();
	}
	public QCheckbox() {
		super();
		init();
	}
	protected void serverCheckedChanged(final boolean checked) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].initValue(");
			writeObject(""+checked);
			write(");\n");
		}
	}
	public void handle(JSONObject post) throws IOException {
		boolean checked=post.getBoolean("checked");
		this.checked.setPropertyFromClient(checked);
	}

	@Override
	public void doInitJSObject() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tnew QCheckbox(page, \"");
			writeObject(id);
			write("\").initValue(");
			writeJSValue(""+checked.getProperty());
			write(");\n");
			checked.serverChangedEvent.addListener(new UtilEventListener<Boolean>() {
				@Override
				public void eventHappened(Boolean msg) {
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
