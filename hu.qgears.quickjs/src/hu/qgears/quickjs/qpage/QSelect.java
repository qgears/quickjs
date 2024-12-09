package hu.qgears.quickjs.qpage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilComma;
import hu.qgears.commons.UtilEventListener;

abstract public class QSelect extends QComponent {
	public final QProperty<List<String>> options=new QProperty<>((List<String>)new ArrayList<String>());
	public final QProperty<Integer> selected=new QProperty<>();
	public QSelect(IQContainer parent, String id) {
		super(parent, id);
		init();
	}
	public QSelect(IQContainer parent) {
		super(parent);
		init();
	}
	public QSelect() {
		super();
		init();
	}
	
	protected void serverOptionsChanged(final List<String> msg)
	{
		try(NoExceptionAutoClosable c=activateJS())
		{
			sendOptions(msg);
		}
	}


	private void sendOptions(List<String> msg) {
		write("\tpage.components['");
		writeJSValue(id);
		write("'].setOptions([");
		UtilComma c=new UtilComma(", ");
		for(String option: msg)
		{
			writeObject(c.getSeparator());
			write("\"");
			writeJSValue(option);
			write("\"");
		}
		write("]);\n");
	}
	
	private void sendSelected() {
		write("\tpage.components['");
		writeJSValue(id);
		write("'].setSelected(\"");
		writeObject(selected.getProperty());
		write("\");\n");
	}


	@Override
	final public void doInitJSObject() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tnew ");
			writeObject(getClass().getSimpleName());
			write("(page, \"");
			writeObject(id);
			write("\");\n");
			sendOptions(options.getProperty());
			if(selected.getProperty()!=null)
			{
				sendSelected();
			}
			options.serverChangedEvent.addListener(new UtilEventListener<List<String>>() {
				@Override
				public void eventHappened(List<String> msg) {
					serverOptionsChanged(msg);
				}
			});
			selected.serverChangedEvent.addListener(new UtilEventListener<Integer>() {
				@Override
				public void eventHappened(Integer msg) {
					try(NoExceptionAutoClosable c=activateJS())
					{
						sendSelected();
					}
				}
			});
		}
	}

	@Override
	final public void handle( JSONObject post) throws IOException {
		try(NoExceptionAutoClosable c=activateJS())
		{
			try {
				if(post.has("selected"))
				{
					selected.setPropertyFromClient(post.getInt("selected"));
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
