package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEventListener;

public class QSvgContainer extends QComponent
{
	public static class Geometry
	{
		public int width=100;
		public int height=100;
		public double vbX=0;
		public double vbY=0;
		public double vbW=100;
		public double vbH=100;
		@Override
		public Geometry clone() {
			Geometry ret=new Geometry();
			ret.width=width;
			ret.height=height;
			ret.vbX=vbX;
			ret.vbY=vbY;
			ret.vbW=vbW;
			ret.vbH=vbH;
			return ret;
		}
		public void matchGeometry() {
			double targetRate=((double)width)/height;
			if(vbW/vbH>targetRate)
			{
				vbH=vbW/targetRate;
			}else
			{
				vbW=vbH*targetRate;
			}
		}
	}
	public final QProperty<String> svg=new QProperty<>();
//	public final QProperty<Rectangle> viewBox=new QProperty<>(new Rectangle(0,0,100,100));
//	public final QProperty<Point> size=new QProperty<>(new Point(100,100));
	public final QProperty<Geometry> geometry=new QProperty<>(new Geometry());
	// public final UtilEvent<String> enterPressed=new UtilEvent<>();
	public QSvgContainer(IQContainer container, String identifier) {
		super(container, identifier);
		init();
	}
	public QSvgContainer(IQContainer container) {
		super(container);
		init();
	}
	public QSvgContainer() {
		super();
		init();
	}
	protected void serverSvgChanged(final String msg) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].initValue(\"");
			writeJSValue(msg);
			write("\");\n");
		}
	}
	private void viewBoxChanged(Geometry g) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].setViewBox(");
			writeObject(g.vbX);
			write(",");
			writeObject(g.vbY);
			write(",");
			writeObject(g.vbW);
			write(",");
			writeObject(g.vbH);
			write(");\npage.components['");
			writeJSValue(id);
			write("'].setSize(");
			writeObject(g.width);
			write(",");
			writeObject(g.height);
			write(");\n");
		}
	}
//	@Override
//	public void generateHtmlObject() {
//		write("<svg id=\"");
//		writeObject(id);
//		write("</svg>\n");
//	}

	public void handle(JSONObject post) throws IOException {
/*		String ntext=JSONHelper.getStringSafe(post,"text");
		if(ntext!=null)
		{
			svg.setPropertyFromClient(ntext);
		}
		String exter=JSONHelper.getStringSafe(post, "enter");
		if(exter!=null)
		{
			enterPressed.eventHappened(exter);
		}
		*/
	}

	@Override
	public void doInitJSObject() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tnew QSvgContainer(page, \"");
			writeObject(id);
			write("\").initValue(\"");
			writeJSValue(svg.getProperty());
			write("\")\n");
			if(geometry.getProperty()!=null)
			{
				write("\t.setViewBox(");
				writeObject(geometry.getProperty().vbX);
				write(",");
				writeObject(geometry.getProperty().vbY);
				write(",");
				writeObject(geometry.getProperty().vbW);
				write(",");
				writeObject(geometry.getProperty().vbH);
				write(")\n");
			}
			if(geometry.getProperty()!=null)
			{
				write("\t.setSize(");
				writeObject(geometry.getProperty().width);
				write(",");
				writeObject(geometry.getProperty().height);
				write(")\n");
			}
			write("\t;\n");
			svg.serverChangedEvent.addListener(new UtilEventListener<String>() {
				@Override
				public void eventHappened(String msg) {
					serverSvgChanged(msg);
				}
			});
			geometry.serverChangedEvent.addListener(vb->viewBoxChanged(vb));
		}
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
