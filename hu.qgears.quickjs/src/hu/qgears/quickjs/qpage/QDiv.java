package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;

public class QDiv extends QComponent
{
	public QDiv(IQContainer container, String identifier) {
		super(container, identifier);
		if(getClass()==QDiv.class)
		{
			init();
		}
	}
	public QDiv(IQContainer container) {
		super(container);
		if(getClass()==QDiv.class)
		{
			init();
		}
	}
	public QDiv() {
		super();
		if(getClass()==QDiv.class)
		{
			init();
		}
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
