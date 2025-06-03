package hu.qgears.quickjs.serialization.generator;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.Pair;
import hu.qgears.quickjs.serialization.ClassFullName;
import hu.qgears.quickjs.serialization.CommunicationModel;
import hu.qgears.quickjs.serialization.IQCallContext;
import hu.qgears.quickjs.serialization.IRemotingBase;
import hu.qgears.quickjs.serialization.IRemotingCallback;
import hu.qgears.quickjs.serialization.SerializeBase;

public class ProcessInterface {
	private Logger log=LoggerFactory.getLogger(getClass());
	Class<?> claRoot;
	Class<?> cla;
	public ProcessInterface(Class<?> cla, CmdArgs args) {
		this.claRoot=cla;
		this.args=args;
	}
	private CommunicationModel model=new CommunicationModel();
	private CmdArgs args;

	public void process() throws Exception {
		for(Method m: getMethods(claRoot))
		{
			cla=m.getReturnType();
			if(!IRemotingBase.class.isAssignableFrom(cla))
			{
				System.out.println("Serializable type: "+cla.getName());
				processType(cla);
				continue;
				// throw new RuntimeException("Return type is not remoting type: "+cla.getName()+" is not instanceof "+IRemotingBase.class.getName());
			}
			System.out.println("Method: "+m+" Class: "+cla);
			processOne(m.getName());
		}
		generateImple1();
	}
	
	public static Method[] getMethods(Class<?> claRoot2) {
		Method[] methods=claRoot2.getMethods();
		List<Method> ms=new ArrayList<>();
		for(Method m: methods)
		{
			ms.add(m);
		}
		Collections.sort(ms, new Comparator<Method>() {
			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return ms.toArray(new Method[0]);
	}

	private void processOne(String iface) throws Exception {
		model.serverInterfaces.add(cla);
		for(Method m: cla.getMethods())
		{
			try {
				processType(m.getGenericReturnType());
			} catch (Exception e) {
				log.error("generate: "+m.getGenericReturnType(), e);
			}
			for(Type t: m.getGenericParameterTypes())
			{
				processType(t);
			}
		}
		generateImpl(iface);
	}

	private void generateImple1() throws Exception
	{
		new RemotingServerTemplate(new ClassFullName(args.packageName+".RemotingServer"), model, claRoot)
			.setImplements(claRoot)
			.generateTo(args.outputFolder);
		ClassFullName cfn=new ClassFullName(args.packageName+".Serialize");
		new SerializationTemplate(model, cfn).generateTo(args.outputFolder);
		
		cfn=new ClassFullName(args.packageName+"."+args.classPrefix+claRoot.getSimpleName());
		new AllTemplate(args, cfn, model, claRoot, null).setIsinterface(true).generateTo(args.outputFolder);
		ClassFullName cfnImpl=new ClassFullName(args.packageName+"."+args.classPrefix+claRoot.getSimpleName()+"Impl");
		new AllTemplate(args, cfnImpl, model, claRoot, null).setImplementsIface(cfn).generateTo(args.outputFolder);
	}
	private void generateImpl(String iface) throws Exception {
		String name=cla.getSimpleName();
		System.out.println("IFACE name: "+name);
		ClassFullName iremoting=new ClassFullName(args.packageName+"."+args.classPrefix+name);
		new RemotingTemplate(new ClassFullName(args.packageName+"."+args.classPrefix+name+"Impl"), model, cla, iface)
			.setImplements(iremoting)
			.generateTo(args.outputFolder);
		new RemotingTemplate(iremoting, model, cla, iface)
			.setIsinterface(true)
			.generateTo(args.outputFolder);
	}

	private void processType(Type t) {
		Class<?> c;
		if(t instanceof ParameterizedType)
		{
			ParameterizedType pt=(ParameterizedType) t;
			c=(Class<?>)pt.getRawType();
			Type[] typeArgs=pt.getActualTypeArguments();
			for(Type ts:typeArgs)
			{
				processType(ts);
			}	
		}else if(t instanceof WildcardType)
		{
			return;
		}
		else
		{
			c=(Class<?>) t;
		}
		while(c.isArray())
		{
			c=c.getComponentType();
		}
		if(c == IQCallContext.class)
		{
			return;
		}
		if(c == IRemotingCallback.class)
		{
			return;
		}
		if(c == NoExceptionAutoClosable.class)
		{
			return;
		}
		if(SerializeBase.handles(c))
		{
			return;
		}
		if(model.dtos.add((Class<?>)getWrapperClass(c)))
		{
			for(Field f: SerializationTemplate.getFieldsToSerialize(c))
			{
				Type subtype=f.getGenericType();
				processType(subtype);
			}
			for(Pair<Method, Method> p: SerializationTemplate.getGetSettersToSerialize(c))
			{
				Method m=p.getA();
				Type subtype=m.getGenericReturnType();
				processType(subtype);
			}
		}
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
			if(p.getType() == IQCallContext.class)
			{
				continue;
			}
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

	public static int process(CmdArgs cmargs) throws Exception {
		List<URL> urls=new ArrayList<>();
		for(File f: cmargs.classPath)
		{
			urls.add(f.toURI().toURL());
		}
		ClassLoader cl=new URLClassLoader(urls.toArray(new URL[] {}));
		Class<?> cla=cl.loadClass(cmargs.iface);
		new ProcessInterface(cla, cmargs).process();
		return 0;
	}
}
