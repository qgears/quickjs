package hu.qgears.quickjs.qpage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilComma;
import hu.qgears.commons.UtilEventListener;

abstract public class QSelect extends QComponent {

	public final QProperty<List<String>> options=new QProperty<>((List<String>)new ArrayList<String>());
	public final QProperty<Integer> selected=new QProperty<>();
	public QSelect(QPage page0, String id) {
		super(page0, id);
		options.serverChangedEvent.addListener(new UtilEventListener<List<String>>() {
			
			@Override
			public void eventHappened(List<String> msg) {
				serverOptionsChanged(msg);
			}
		});
		selected.serverChangedEvent.addListener(new UtilEventListener<Integer>() {
			@Override
			public void eventHappened(Integer msg) {
				if(page.inited)
				{
					setWriter(page.getCurrentTemplate().getWriter());
					sendSelected();
					setWriter(null);
				}
			}
		});
	}
	
	protected void serverOptionsChanged(final List<String> msg)
	{
		if(page.inited)
		{
			setWriter(page.getCurrentTemplate().getWriter());
			sendOptions(msg);
			setWriter(null);
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
	final public void init(HtmlTemplate parent) {
		setWriter(parent.getWriter());
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
		setWriter(null);
	}

	@Override
	final public void handle(HtmlTemplate parent, IInMemoryPost post) throws IOException {
		setWriter(parent.getWriter());
		try {
			selected.setPropertyFromClient(Integer.parseInt(post.getParameter("selected")));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setWriter(null);
	}
}
