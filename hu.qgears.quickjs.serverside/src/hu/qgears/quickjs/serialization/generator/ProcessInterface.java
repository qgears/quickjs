package hu.qgears.quickjs.serialization.generator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import hu.qgears.quickjs.serialization.ClassFullName;
import hu.qgears.quickjs.serialization.CmdArgs;
import hu.qgears.quickjs.serialization.CommunicationModel;

public class ProcessInterface {
	Class<?> cla;
	public ProcessInterface(Class<?> cla) {
		this.cla=cla;
	}
	private CommunicationModel model=new CommunicationModel();
	private CmdArgs args=new CmdArgs();

	public void process(Class<?> cla) throws Exception {
		model.serverInterfaces.add(cla);
		for(Method m: cla.getMethods())
		{
			processType(m.getGenericReturnType());
			for(Type t: m.getGenericParameterTypes())
			{
				processType(t);
			}
		}
		generateImpl();
	}
	
	private void generateImpl() throws Exception {
		ClassFullName iremoting=new ClassFullName(args.packageName+".IRemoting");
		new RemotingTemplate(new ClassFullName(args.packageName+".Remoting"), model, cla)
			.setImplements(iremoting)
			.generateTo(args.outputFolder);
		new RemotingTemplate(iremoting, model, cla)
		.setIsinterface(true)
		.generateTo(args.outputFolder);
		new RemotingServerTemplate(new ClassFullName(args.packageName+".RemotingServer"), model, cla)
			.setImplements(cla)
			.generateTo(args.outputFolder);
		ClassFullName cfn=new ClassFullName(args.packageName+".Serialize");
		new SerializationTemplate(model, cfn).generateTo(args.outputFolder);
	}

	private void processType(Type t) {
		Class<?> c;
		if(t instanceof ParameterizedType)
		{
			ParameterizedType pt=(ParameterizedType) t;
			c=(Class<?>)pt.getRawType();
		}else
		{
			c=(Class<?>) t;
		}
		// System.out.println("Type to process: "+c+" "+getWrapperClass(c));
		model.dtos.add((Class<?>)getWrapperClass(c));
	}
	public static Type getWrapperClass(Type primitive)
	{
		if(!(primitive instanceof Class<?>) || !((Class<?>)primitive).isPrimitive())
		{
			return primitive;
		}
		if(java.lang.Boolean.TYPE==primitive)
		{
			return Boolean.class;
		}
		if(java.lang.Integer.TYPE==primitive)
		{
			return Integer.class;
		}
		if(java.lang.Void.TYPE==primitive)
		{
			return java.lang.Void.class;
		}
		if(java.lang.Character.TYPE==primitive)
		{
			return java.lang.Character.class;
		}
		if(java.lang.Byte.TYPE==primitive)
		{
			return java.lang.Byte.class;
		}
		if(java.lang.Short.TYPE==primitive)
		{
			return java.lang.Short.class;
		}
		if(java.lang.Long.TYPE==primitive)
		{
			return java.lang.Long.class;
		}
		if(java.lang.Float.TYPE==primitive)
		{
			return java.lang.Float.class;
		}
		if(java.lang.Double.TYPE==primitive)
		{
			return java.lang.Double.class;
		}
		if(java.lang.Void.TYPE==primitive)
		{
			return java.lang.Void.class;
		}
		throw new RuntimeException("unknown primitive: "+primitive);
	}
	public static String methodPrototype(Method m)
	{
		StringBuilder ret=new StringBuilder();
		ret.append(m.getName());
		ret.append("(");
		boolean first=true;
		for(Parameter p: m.getParameters())
		{
			if(!first)
			{
				ret.append(",");
			}
			first=false;
			String name=p.getType().getName();
			ret.append(name);
		}
		ret.append(")");
		return ret.toString();
		
	}
}
