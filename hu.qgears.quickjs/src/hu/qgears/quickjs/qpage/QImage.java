package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEventListener;

public class QImage extends QComponent
{
	public final QProperty<String> src=new QProperty<>();
	private UtilEventWithListenerTrack<QImage> onload;
	public QImage(IQContainer parent, String identifier) {
		super(parent, identifier);
		init();
	}
	public QImage(IQContainer parent) {
		super(parent);
		init();
	}
//	@Override
//	public void generateHtmlObject() {
//		write("<img id=\"");
//		writeObject(id);
//		write("\"></img>\n");
//	}

	public void handle(JSONObject post) throws IOException {
		if(post.has("type"))
		{
			switch (post.getString("type")) {
			case "onload":
				if(onload!=null)
				{
					onload.eventHappened(this);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void doInitJSObject() {
		write("\tnew QImage(page, \"");
		writeObject(id);
		write("\")");
		if(src.getProperty()!=null){
			write(".initSrc(\"");
			writeJSValue(src.getProperty());
			write("\")");
		}
		write(";\n");
		if(onload!=null)
		{
			updateOnloadListeners();
		}
		src.serverChangedEvent.addListener(new UtilEventListener<String>() {
			@Override
			public void eventHappened(String msg) {
				srcChanged(msg);
			}
		});
	}
	protected void srcChanged(final String msg) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].initSrc(\"");
			writeJSValue(msg);
			write("\");\n");
		}
	}
	public UtilEventWithListenerTrack<QImage> getOnload() {
		if(onload==null)
		{
			onload=new UtilEventWithListenerTrack<>(e->{
				if(inited)
				{
					updateOnloadListeners();
				}
			});
		}
		return onload;
	}
	private void updateOnloadListeners() {
		try(NoExceptionAutoClosable c=activateJS())
		{	
			write("page.components['");
			writeJSValue(id);
			write("'].setHasOnloadListener(");
			writeObject(onload.getNListeners()>0);
			write(");\n");
		}
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
