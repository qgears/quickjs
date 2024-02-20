package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;

/**
 * A smart link implementation: normal link <a href="..."> with feature to open content
 * in a single page implementation.
 * html5_history.asciidoc
 */
public class QLink extends QComponent
{
	public final UtilEvent<QLink> clicked=new UtilEvent<>();
	public final QProperty<String> href;
	/**
	 * Is this link server side handled? True means normal left click navigations
	 * will trigger an event on the server and default reaction is disabled in the browser - ie. no navigation will happen.
	 * Open in new tab, etc will work as default.
	 * Default value is true - because this is the feature why this object exists.
	 */
	public final QProperty<Boolean> serverHandled=new QProperty<Boolean>(true);
	public QLink(IQContainer container, String identifier, String href) {
		super(container, identifier);
		this.href=new QProperty<>(href);
	}
	public QLink(IQContainer container, String href) {
		super(container);
		this.href=new QProperty<>(href);
	}
	/**
	 * Chainable shorthand for .clicked.addListener();
	 * @param clickHandler
	 * @return this self object for chaining calls
	 */
	public QLink addClickHandler(UtilEventListener<QLink> clickHandler)
	{
		clicked.addListener(clickHandler);
		return this;
	}
	
	public void generateHtmlObject() {
		write("<a id=\"");
		writeObject(id);
		write("\"></a>\n");
	}

	public void handle(HtmlTemplate parent, JSONObject post) throws IOException {
		clicked.eventHappened(this);
	}

	@Override
	public void doInitJSObject() {
		String currentHref=href.getProperty();
		setParent(page.getCurrentTemplate());
		write("\tnew QLink(page, \"");
		writeObject(id);
		write("\")");
		writeSetHref(currentHref);
		write(".setServerHandled(");
		writeObject(serverHandled.getProperty());
		write(");\n");
		setParent(null);
		href.serverChangedEvent.addListener(e->{
			try(ResetOutputObject roo=setParent(page.getCurrentTemplate()))
			{
				write("page.components['");
				writeJSValue(id);
				write("']");
				writeSetHref(href.getProperty());
				write(";\n");
			}
		});
		serverHandled.serverChangedEvent.addListener(e->{
			try(ResetOutputObject roo=setParent(page.getCurrentTemplate()))
			{
				write("page.components['");
				writeJSValue(id);
				write("'].setServerHandled(\"");
				writeObject(serverHandled.getProperty());
				write("\");\n");
			}
		});
	}
	private void writeSetHref(String currentHref) {
		if(currentHref!=null){
			write(".setHref(\"");
			writeJSValue(currentHref);
			write("\")");
		}else{
			write(".setHref(null)");
		}
	}
	public void setDisabled(boolean b) {
		// TODO Auto-generated method stub
		
	}
}
