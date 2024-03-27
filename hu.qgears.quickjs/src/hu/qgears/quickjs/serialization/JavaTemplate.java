package hu.qgears.quickjs.serialization;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import hu.qgears.commons.UtilFile;

abstract public class JavaTemplate {
	protected Writer writer=new StringWriter();
	protected ClassFullName cfn;
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
	protected void writeClass(ClassFullName cfn) throws IOException {
		writer.write(cfn.fqn);
	}
	protected void write(String string) throws IOException {
		writer.write(string);
	}
	abstract public String generate() throws Exception;
	public void generateTo(File outputFolder) throws Exception {
		String s=generate();
		File f=new File(outputFolder, cfn.getPath());
		f.getParentFile().mkdirs();
		UtilFile.saveAsFile(f, s);
	}
}
