package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;

public class QButton extends QComponent
{
	public final UtilEvent<QButtonEvent> clicked=new UtilEvent<>();
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
	public final UtilEventWithListenerTrack<QButtonEvent> rightClicked=new UtilEventWithListenerTrack<>(e->{
		if(inited)
		{
			if(e.getNListeners()==1)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					write("\tpage.components[\"");
					writeObject(id);
					write("\"].addRightClickListener(true);\n");
				}
			}else if(e.getNListeners()==0)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					write("\tpage.components[\"");
					writeObject(id);
					write("\"].addRightClickListener(false);\n");
				}
			}
		}
	});
	/**
	 * Mouse down event. In case a listener is added then mouse down event listening is activated.
	 */
	public final UtilEventWithListenerTrack<QButtonEvent> mouseDown=new UtilEventWithListenerTrack<>(e->{
		if(inited)
		{
			if(e.getNListeners()==1)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					write("\tpage.components[\"");
					writeObject(id);
					write("\"].addMouseDownListener(true);\n");
				}
			}else if(e.getNListeners()==0)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					write("\tpage.components[\"");
					writeObject(id);
					write("\"].addMouseDownListener(false);\n");
				}
			}
		}
	});
	public QButton(IQContainer container, String identifier) {
		super(container, identifier);
		init();
	}
	public QButton(IQContainer container) {
		super(container);
		init();
	}
	
	public QButton() {
		super();
		init();
	}

	public void handle(JSONObject post) throws IOException {
		QButtonEvent ev=new QButtonEvent(this, post);
		switch (ev.button) {
		case 0:
			clicked.eventHappened(ev);
			break;
		case 3:
			rightClicked.eventHappened(ev);
			break;
		case 4:
			mouseDown.eventHappened(ev);
			break;
		default:
			break;
		}
	}

	@Override
	public void doInitJSObject() {
		try(NoExceptionAutoClosable c=activateJS())
		{
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
					innerHtmlChanged(msg);
				}
			});
		}
	}
	protected void innerHtmlChanged(final String msg) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].setInnerHtml(\"");
			writeJSValue(msg);
			write("\");\n");
		}
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
