package hu.qgears.quickjs.serialization.generator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.helpers.PromiseImpl;
import hu.qgears.quickjs.serialization.CallbackRegistryEntry;
import hu.qgears.quickjs.serialization.ClassFullName;
import hu.qgears.quickjs.serialization.CommunicationModel;
import hu.qgears.quickjs.serialization.IQCallContext;
import hu.qgears.quickjs.serialization.IRemotingCallback;
import hu.qgears.quickjs.serialization.JavaTemplate;
import hu.qgears.quickjs.serialization.RemotingBase;

public class RemotingTemplate extends JavaTemplate {
	Class<?> cla;
	private boolean isinterface;
	ClassFullName implementsIface;
	String iface;
	public RemotingTemplate(ClassFullName cfn, CommunicationModel model, Class<?> cla, String iface) {
		this.cla=cla;
		this.cfn=cfn;
		this.iface=iface;
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
			boolean isCallbackSubscribe=false;
			for(Parameter p: m.getParameters())
			{
				if(p.getType()==IRemotingCallback.class)
				{
					if(isCallbackSubscribe)
					{
						throw new RuntimeException("Two callback in one method: "+m);
					}
					if(m.getReturnType()!=NoExceptionAutoClosable.class)
					{
						throw new RuntimeException("Callback subscription must return "+NoExceptionAutoClosable.class.getName()+": "+m);
					}
					isCallbackSubscribe=true;
				}
			}
			if(!isinterface)
			{
				write("\t@Override\n");
			}
			write("\tpublic ");
			if(isCallbackSubscribe)
			{
				writeClass(CallbackRegistryEntry.class);//NB
				write(" ");
			}else
			{
				writeClass(Promise.class);//NB
				write("<");
				writeClass(ProcessInterface.getWrapperClass(m.getGenericReturnType()));
				write("> ");
			}
			writeObject(m.getName());
			write(" (");
			boolean first=true;
			for(Parameter p: m.getParameters())
			{
				if(p.getType()==IQCallContext.class)
				{
					continue;
				}
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
				write("\t{\n");
				if(isCallbackSubscribe) {
					write("\t\t");
					writeClass(CallbackRegistryEntry.class);//NB
					write(" ret;\n");
				}
				write("\t\tString methodPrototype=\"");
				writeObject(ProcessInterface.methodPrototype(m));
				write("\";\n\t\tObject[] argumentsToSerialize=new Object[]{");
			first=true;
			for(Parameter p: m.getParameters())
			{
				if(p.getType()==IQCallContext.class)
				{
					continue;
				}
				if(!first)
				{
					write(", ");
				}
				first=false;
				String name=p.getName();
				if(p.getType()==IRemotingCallback.class)
				{
					write("ret=registerCallback(methodPrototype, ");
					writeObject(name);
					write(")");
				}else
				{
					writeObject(name);
				}
			}
			write(" };\n");
			if(isCallbackSubscribe) {
				write("\t\tret.setArgs(argumentsToSerialize);\n");
			}
			write("\t\t");
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
			write(">)(Object) remotingReturnValue,\n\t\t\t\"");
			writeObject(iface);
			write("\", methodPrototype, argumentsToSerialize, ");
			writeObject(isCallbackSubscribe);
			write(");\n\t\treturn ");
			writeObject(isCallbackSubscribe?"ret":"remotingReturnValue");
			write(";\n\t}\n");
			}
		}
	}
	public RemotingTemplate setImplements(ClassFullName iremoting) {
		implementsIface=iremoting;
		return this;
	}
}
