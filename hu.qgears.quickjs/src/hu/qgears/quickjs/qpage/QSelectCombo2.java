package hu.qgears.quickjs.qpage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import hu.qgears.commons.UtilComma;
import hu.qgears.commons.UtilEventListener;

/**
 * Combo box selector implementation based on div based implementation.
 */
public class QSelectCombo2 extends QComponent {
	private Logger log=Logger.getLogger(getClass());
	public interface OptionCreator
	{
		default String createOption(int optionIndex)
		{
			HtmlTemplate t=new HtmlTemplate();
			createOption(t, optionIndex);
			return t.getWriter().toString();
		}
		void createOption(HtmlTemplate parrent, int optionIndex);
	}
	public final QProperty<List<OptionCreator>> options=new QProperty<>((List<OptionCreator>)new ArrayList<OptionCreator>());
	public final QProperty<Integer> selected=new QProperty<>();
	public QSelectCombo2(IQContainer parent, String id) {
		super(parent, id);
	}
	public QSelectCombo2(IQContainer parent) {
		super(parent);
	}
	
	protected void serverOptionsChanged(final List<OptionCreator> msg)
	{
		if(page.inited)
		{
			try(ResetOutputObject roo=setParent(page.getCurrentTemplate()))
			{
				sendOptions(msg);
			}
		}
	}


	private void sendOptions(List<OptionCreator> msg) {
		write("\tpage.components['");
		writeJSValue(id);
		write("'].setOptions([");
		UtilComma c=new UtilComma(", ");
		int i=0;
		for(OptionCreator option: msg)
		{
			writeObject(c.getSeparator());
			write("\"");
			writeJSValue(option.createOption(i));
			write("\"");
			i++;
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
		options.serverChangedEvent.addListener(new UtilEventListener<List<OptionCreator>>() {
			@Override
			public void eventHappened(List<OptionCreator> msg) {
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

	@Override
	final public void handle(HtmlTemplate parent, JSONObject post) throws IOException {
		try(ResetOutputObject roo=setParent(parent))
		{
			if(post.has("selected"))
			{
				selected.setPropertyFromClient(post.getInt("selected"));
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	@Override
	public void generateHtmlObject() {
		write("<div id=\"");
		writeObject(getId());
		write("\"></div>\n");
	}
}
