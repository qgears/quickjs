package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEventListener;

public class QProgressBar extends QComponent {
	
	public final QProperty<Double> value=new QProperty<Double>(0.0);
	public final QProperty<Double> max=new QProperty<Double>(100.0);
	

	public QProgressBar() {
		super();
		init();
	}
	public QProgressBar(IQContainer container, String id) {
		super(container, id);
		init();
	}
	public QProgressBar(IQContainer container) {
		super(container);
		init();
	}
	public QProgressBar(String id) {
		super(id);
		init();
	}
	@Override
	protected void doInitJSObject() {
		write("\tnew QProgressBar(page, \"");
		writeObject(id);
		write("\").setValue(");
		writeObject(value.getProperty());
		write(").setMax(");
		writeObject(max.getProperty());
		write(");");
		value.serverChangedEvent.addListener(new UtilEventListener<Double>() {
			@Override
			public void eventHappened(Double msg) {
				valueChanged(msg);
			}
		});
		max.serverChangedEvent.addListener(new UtilEventListener<Double>() {
			@Override
			public void eventHappened(Double msg) {
				maxChanged(msg);
			}
		});
	}
	protected void valueChanged(final Double msg) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].setValue(\"");
			writeObject(msg);
			write("\");\n");
		}
	}
	protected void maxChanged(final Double msg) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].setMax(\"");
			writeObject(msg);
			write("\");\n");
		}
	}

	@Override
	public void handle(JSONObject post) throws IOException {
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
