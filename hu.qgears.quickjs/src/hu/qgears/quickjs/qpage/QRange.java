package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEventListener;

public class QRange extends QComponent
{
	public final QProperty<Integer> value=new QProperty<>(0);
	/**
	 * The value currently set by the movement of current input
	 * (on "input" event) but not finalized yet by the "change" event.
	 */
	public final QProperty<Integer> inputValue=new QProperty<>(0);
	public final QProperty<Integer> min=new QProperty<>(0);
	public final QProperty<Integer> max=new QProperty<>(100);
	public QRange(IQContainer container, String identifier) {
		super(container, identifier);
		init();
	}
	public QRange(IQContainer container) {
		super(container);
		init();
	}
	public QRange() {
		super();
		init();
	}
	protected void serverCheckedChanged() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("globalQPage.components['");
			writeJSValue(id);
			write("'].setValue(");
			writeObject(""+value.getProperty());
			write(",");
			writeObject(""+min.getProperty());
			write(",");
			writeObject(""+max.getProperty());
			write(");\n");
		}
	}
//	@Override
//	public void generateHtmlObject() {
//		write("<input id=\"");
//		writeObject(id);
//		write("\" type=\"range\">\n");
//	}
	@Override
	public void handle(JSONObject post) throws IOException {
		String event=post.getString("event");
		int value=post.getInt("value");
		switch(event)
		{
		case "input":
			this.inputValue.setPropertyFromClient(value);
			break;
		case "change":
			this.inputValue.setPropertyFromClient(value);
			this.value.setPropertyFromClient(value);
			break;
		}
	}

	@Override
	public void doInitJSObject() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tnew QRange(page, \"");
			writeObject(id);
			write("\");\n");
			value.serverChangedEvent.addListener(new UtilEventListener<Integer>() {
				@Override
				public void eventHappened(Integer msg) {
					serverCheckedChanged();
				}
			});
			min.serverChangedEvent.addListener(new UtilEventListener<Integer>() {
				@Override
				public void eventHappened(Integer msg) {
					serverCheckedChanged();
				}
			});
			max.serverChangedEvent.addListener(new UtilEventListener<Integer>() {
				@Override
				public void eventHappened(Integer msg) {
					serverCheckedChanged();
				}
			});
			serverCheckedChanged();
		}
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
