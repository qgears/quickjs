package hu.qgears.quickjs.serialization.generator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import hu.qgears.quickjs.serialization.ClassFullName;
import hu.qgears.quickjs.serialization.CommunicationModel;
import hu.qgears.quickjs.serialization.JavaTemplate;
import hu.qgears.quickjs.serialization.RemotingServer;

public class RemotingServerTemplate extends JavaTemplate {
	Class<?> cla;
	Class<?> implementsIface;
	public RemotingServerTemplate(ClassFullName cfn, CommunicationModel model, Class<?> cla) {
		this.cla=cla;
		this.cfn=cfn;
	}
	@Override
	public String generate() throws IOException {
		write("package ");
		writeObject(cfn.getPackageName());
		write(";\npublic ");
		{
			write("class ");
			writeObject(cfn.getSimpleName());
			write(" extends ");
			writeClass(RemotingServer.class);//NB
			write(" {\n\tprivate ");
			writeClass(implementsIface);//NB
			write(" publishedObject;\n\tpublic void setRpublishedObject(");
			writeClass(implementsIface);//NB
			write(" publishedObject) {\n\t\tthis.publishedObject = publishedObject;\n\t}\n");
		}
		generateThisIf(cla);
		write("}\n");
		return getWriter().toString();
	}
	private void generateThisIf(Class<?> cla) throws IOException {
		write("\t@SuppressWarnings(\"unchecked\")\n\t@Override\n\tpublic Object callFromClient(String methodPrototype, Object[] arg)\n\t{\n\t\tswitch(methodPrototype)\n\t\t{\n");
		for(Method m: cla.getMethods())
		{
			boolean void_=m.getReturnType()==Void.TYPE;
			write("\t\t\tcase \"");
			writeObject(ProcessInterface.methodPrototype(m));
			write("\":\n\t\t\t{\n");
			if(!void_)
			{
				write("\t\t\t\treturn ");
			} else {
				write("\t\t\t\t");
			}
			write("publishedObject.");
			writeObject(m.getName());
			write("(");
			boolean first=true;
			int index=0;
			for(Parameter p: m.getParameters())
			{
				if(!first)
				{
					write(", ");
				}
				first=false;
				write("(");
				writeClass(p.getParameterizedType());//NB
				write(") arg[");
				writeObject(""+index);
				write("]");
				index++;
			}
			write(");\n");
			if(void_)
			{
				write("\t\t\t\treturn null;\n");
			}
			write("\t\t\t}\n");
		}
		write("\t\t\tdefault:\n\t\t\t\treturn super.callFromClient(methodPrototype, arg);\n\t\t}\n\t}\n");
	}
	public RemotingServerTemplate setImplements(Class<?> cla) {
		implementsIface=cla;
		return this;
	}
}
