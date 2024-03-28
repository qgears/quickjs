package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;

public class QDiv extends QComponent
{
	public QDiv(IQContainer container, String identifier) {
		super(container, identifier);
		init();
	}
	public QDiv(IQContainer container) {
		super(container);
		init();
	}
	public QDiv() {
		super();
		init();
	}
	public void handle(JSONObject post) throws IOException {
	}
	@Override
	public void doInitJSObject() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tnew QDiv(page, \"");
			writeObject(id);
			write("\");\n");
		}
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
