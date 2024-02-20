package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;

public class QButton extends QComponent
{
	public final UtilEvent<QButton> clicked=new UtilEvent<>();
	/**
	 * Set the HTML content of the "controlled node".
	 * Default value is null and that means no update on the node.
	 */
	public final QProperty<String> innerhtml=new QProperty<>(null);
	/**
	 * Right clicked event. In case a listener is added then right click listening is activated and
	 * right click event propagation is stopped.
	 */
	@SuppressWarnings("resource")
	public final UtilEventWithListenerTrack<QButton> rightClicked=new UtilEventWithListenerTrack<>(e->{
		if(inited)
		{
			if(e.getNListeners()==1)
			{
				setParent(page.getCurrentTemplate());
				write("\tpage.components[\"");
				writeObject(id);
				write("\"].addRightClickListener(true);\n");
				setParent(null);
			}else if(e.getNListeners()==0)
			{
				setParent(page.getCurrentTemplate());
				write("\tpage.components[\"");
				writeObject(id);
				write("\"].addRightClickListener(false);\n");
				setParent(null);
			}
		}
	});
	/**
	 * Mouse down event. In case a listener is added then mouse down event listening is activated.
	 */
	public final UtilEventWithListenerTrack<QButton> mouseDown=new UtilEventWithListenerTrack<>(e->{
		if(inited)
		{
			if(e.getNListeners()==1)
			{
				setParent(page.getCurrentTemplate());
				write("\tpage.components[\"");
				writeObject(id);
				write("\"].addMouseDownListener(true);\n");
				setParent(null);
			}else if(e.getNListeners()==0)
			{
				setParent(page.getCurrentTemplate());
				write("\tpage.components[\"");
				writeObject(id);
				write("\"].addMouseDownListener(false);\n");
				setParent(null);
			}
		}
	});
	public QButton(IQContainer container, String identifier) {
		super(container, identifier);
	}
	public QButton(IQContainer container) {
		super(container);
	}
	
	public void generateHtmlObject() {
		write("<button id=\"");
		writeObject(id);
		write("\">BUTTON</button>\n");
	}

	public void handle(HtmlTemplate parent, JSONObject post) throws IOException {
		int button=post.getInt("button");
		switch (button) {
		case 0:
			clicked.eventHappened(this);
			break;
		case 3:
			rightClicked.eventHappened(this);
			break;
		case 4:
			mouseDown.eventHappened(this);
			break;
		default:
			break;
		}
	}

	@Override
	public void doInitJSObject() {
		setParent(page.getCurrentTemplate());
		write("\tnew QButton(page, \"");
		writeObject(id);
		write("\")");
		initialDisabledState();
		write(";\n");
		if(rightClicked.getNListeners()>0)
		{
			write("\tpage.components[\"");
			writeObject(id);
			write("\"].addRightClickListener(true);\n");
		}
		if(mouseDown.getNListeners()>0)
		{
			write("\tpage.components[\"");
			writeObject(id);
			write("\"].addMouseDownListener(true);\n");
		}
		if(innerhtml.getProperty()!=null)
		{
			innerHtmlChanged(innerhtml.getProperty());
		}
		innerhtml.serverChangedEvent.addListener(new UtilEventListener<String>() {
			@Override
			public void eventHappened(String msg) {
				try(ResetOutputObject roo=setParent(page.getCurrentTemplate()))
				{
					innerHtmlChanged(msg);
				}
			}
		});
		setParent(null);
	}
	protected void innerHtmlChanged(final String msg) {
		try(ResetOutputObject roo=setParent(page.getCurrentTemplate()))
		{
			write("page.components['");
			writeJSValue(id);
			write("'].setInnerHtml(\"");
			writeJSValue(msg);
			write("\");\n");
		}
	}
}
