package hu.qgears.quickjs.serialization.generator;

import java.lang.reflect.Method;

import hu.qgears.quickjs.serialization.ClassFullName;
import hu.qgears.quickjs.serialization.CommunicationModel;
import hu.qgears.quickjs.serialization.IRemotingBase;
import hu.qgears.quickjs.serialization.IRemotingImplementation;
import hu.qgears.quickjs.serialization.JavaTemplate;
import hu.qgears.quickjs.serialization.RemotingAllBase;

public class AllTemplate extends JavaTemplate {
	Class<?> cla;
	private boolean isinterface;
	ClassFullName implementsIface;
	CmdArgs args;
	String iface;
	public AllTemplate(CmdArgs args, ClassFullName cfn, CommunicationModel model, Class<?> cla, String iface) {
		this.args=args;
		this.cla=cla;
		this.cfn=cfn;
		this.iface=iface;
	}
	public AllTemplate setIsinterface(boolean isinterface) {
		this.isinterface = isinterface;
		return this;
	}
	public AllTemplate setImplementsIface(ClassFullName implementsIface) {
		this.implementsIface = implementsIface;
		return this;
	}
	@Override
	public String generate() throws Exception {
		write("package ");
		writeObject(cfn.getPackageName());
		write(";\npublic ");
		writeObject(isinterface?"interface":"class");
		write(" ");
		writeObject(cfn.getSimpleName());
		if(!isinterface)
		{
			write(" extends ");
			writeClass(RemotingAllBase.class);//NB
		}
		write(" ");
		writeObject(implementsIface==null?"":(" implements "+implementsIface.getSimpleName()));
		write(" \n{\n");
		for(Method m:cla.getMethods())
		{
			Class<?> rettype=m.getReturnType();
			if(IRemotingBase.class.isAssignableFrom(rettype))
			{
				String type=args.classPrefix+rettype.getSimpleName();
				if(!isinterface)
				{
					write("\tprivate ");
					writeObject(type);
					write(" ");
					writeObject(m.getName());
					write(";\n\t@Override\n");
				}
				write("\t");
				writeObject(isinterface?"":"public ");
				writeObject(type);
				write(" ");
				writeObject(m.getName());
				write("()");
				if(isinterface)
				{
					write(";\n");
				}else
				{
					write("{\n\t\tassertNotNull(");
					writeObject(m.getName());
					write(");\n\t\treturn ");
					writeObject(m.getName());
					write(";\n\t}\n");
				}
			}
		}
		if(!isinterface)
		{
			write("\tprotected void assertNotNull(Object o) {\n\t\tif(o==null)\n\t\t{\n\t\t\tthrow new RuntimeException(\"Remoting is uninitialized\");\n\t\t}\n\t}\n\t@Override\n\tpublic void initialize(");
			writeClass(IRemotingImplementation.class);//NB
			write(" remotingImplementation)\n\t{\n\t\tsuper.initialize(remotingImplementation);\n");
			for(Method m:cla.getMethods())
			{
				Class<?> rettype=m.getReturnType();
				if(IRemotingBase.class.isAssignableFrom(rettype))
				{
					String type=args.classPrefix+rettype.getSimpleName()+"Impl";
					write("\t\t");
					writeObject(m.getName());
					write("= new ");
					writeObject(type);
					write("();\n\t\t((");
					writeObject(type);
					write(")");
					writeObject(m.getName());
					write(").setRemotingImplementation(remotingImplementation);\n");
				}
			}
			write("\t}\n");
		}
		write("}\n");
		return writer.toString();
	}
}
