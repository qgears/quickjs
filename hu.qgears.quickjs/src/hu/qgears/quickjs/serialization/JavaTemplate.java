package hu.qgears.quickjs.serialization;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class JavaTemplate {
	protected Writer writer=new StringWriter();
	protected Writer getWriter() {
		return writer;
	}
	protected void writeObject(Object o) throws IOException {
		writer.write(""+o);
	}
	protected void writeClass(Type cla) throws IOException {
		if(cla instanceof Class<?>)
		{
			writer.write(((Class<?>)cla).getName());
		}else if(cla instanceof ParameterizedType)
		{
			ParameterizedType pt=(ParameterizedType) cla;
			writeObject(((Class<?>)pt.getRawType()).getName());
			write("<");
			boolean first=true;
			for(Type t: pt.getActualTypeArguments())
			{
				if(!first)
				{
					write(", ");
				}
				first=false;
				writeClass(t);
			}
			write(">");
		}
	}
	protected void write(String string) throws IOException {
		writer.write(string);
	}
}
