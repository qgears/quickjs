package hu.qgears.quickjs.serialization;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import hu.qgears.quickjs.helpers.PromiseImpl;

public class RemotingTemplate extends JavaTemplate {
	Class<?> cla;
	public RemotingTemplate(CommunicationModel model, Class<?> cla) {
		this.cla=cla;
	}
	public String generate() throws IOException {
		generateThisIf(cla);
		for(Class<?> iface: cla.getInterfaces())
		{
			generateThisIf(iface);
		}
		return getWriter().toString();
	}
	private void generateThisIf(Class<?> cla) throws IOException {
		for(Method m: cla.getMethods())
		{
			write("public Promise<");
			writeClass(ProcessInterface.getWrapperClass(m.getGenericReturnType()));
			write("> ");
			writeObject(m.getName());
			write(" (");
			boolean first=true;
			for(Parameter p: m.getParameters())
			{
				if(!first)
				{
					write(", ");
				}
				first=false;
				writeClass(p.getParameterizedType());
				write(" ");
				String name=p.getName();
				writeObject(name);
			}
			write(")\n{\n\tString methodPrototype=\"");
			writeObject(ProcessInterface.methodPrototype(m));
			write("\";\n\tObject[] argumentsToSerialize=new Object[]{\n\t\t");
			first=true;
			for(Parameter p: m.getParameters())
			{
				if(!first)
				{
					write(", ");
				}
				first=false;
				String name=p.getName();
				writeObject(name);
			}
			write(" };\n\t");
			writeClass(PromiseImpl.class);//NB
			write("<");
			writeClass(ProcessInterface.getWrapperClass(m.getGenericReturnType()));//NB
			write("> remotingReturnValue = new ");
			writeClass(PromiseImpl.class);//NB
			write("<");
			writeClass(ProcessInterface.getWrapperClass(m.getGenericReturnType()));//NB
			write(">();\n\texecuteRemoteCall(remotingReturnValue, methodPrototype, argumentsToSerialize);\n\treturn remotingReturnValue;\n}\n");
		}
	}
}
