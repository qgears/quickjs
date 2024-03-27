package hu.qgears.quickjs.serialization;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.helpers.PromiseImpl;

public class RemotingTemplate extends JavaTemplate {
	Class<?> cla;
	private boolean isinterface;
	ClassFullName implementsIface;
	public RemotingTemplate(ClassFullName cfn, CommunicationModel model, Class<?> cla) {
		this.cla=cla;
		this.cfn=cfn;
	}
	public RemotingTemplate setIsinterface(boolean isinterface) {
		this.isinterface = isinterface;
		return this;
	}
	@Override
	public String generate() throws IOException {
		write("package ");
		writeObject(cfn.getPackageName());
		write(";\n");
		if(!isinterface)
		{
			write("@SuppressWarnings({ \"rawtypes\", \"unchecked\" })\n");
		}
		write("public ");
		if(isinterface)
		{
			write("interface ");
			writeObject(cfn.getSimpleName());
			write(" {\n");
		}else
		{
			write("class ");
			writeObject(cfn.getSimpleName());
			write(" extends ");
			writeClass(RemotingBase.class);//NB
			write(" implements ");
			writeClass(implementsIface);//NB
			write("{\n");
		}
		generateThisIf(cla);
		write("}\n");
		return getWriter().toString();
	}
	private void generateThisIf(Class<?> cla) throws IOException {
		for(Method m: cla.getMethods())
		{
			if(!isinterface)
			{
				write("\t@Override\n");
			}
			write("\tpublic ");
			writeClass(Promise.class);//NB
			write("<");
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
			write(")");
			if(isinterface) {
				write(";\n");
			}else{
				write("\t{\n\t\tString methodPrototype=\"");
				writeObject(ProcessInterface.methodPrototype(m));
				write("\";\n\t\tObject[] argumentsToSerialize=new Object[]{\n\t\t");
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
			write(" };\n\t\t");
			writeClass(PromiseImpl.class);//NB
			write("<");
			writeClass(ProcessInterface.getWrapperClass(m.getGenericReturnType()));//NB
			write("> remotingReturnValue = new ");
			writeClass(PromiseImpl.class);//NB
			write("<");
			writeClass(ProcessInterface.getWrapperClass(m.getGenericReturnType()));//NB
			write(">();\n\t\texecuteRemoteCall(");
			writeClass(ProcessInterface.getWrapperClass(m.getReturnType()));//NB
			write(".class,\n\t\t\t(");
			writeClass(PromiseImpl.class);//NB
			write("<");
			writeClass(ProcessInterface.getWrapperClass(m.getReturnType()));//NB
			write(">)(Object) remotingReturnValue,\n\t\t\tmethodPrototype, argumentsToSerialize);\n\t\treturn remotingReturnValue;\n\t}\n");
			}
		}
	}
	public RemotingTemplate setImplements(ClassFullName iremoting) {
		implementsIface=iremoting;
		return this;
	}
}
