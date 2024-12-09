package hu.qgears.quickjs.serialization.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.Pair;
import hu.qgears.quickjs.serialization.ClassFullName;
import hu.qgears.quickjs.serialization.CommunicationModel;
import hu.qgears.quickjs.serialization.JavaTemplate;
import hu.qgears.quickjs.serialization.SerializeBase;

public class SerializationTemplate extends JavaTemplate {
	CommunicationModel model;
	public SerializationTemplate(CommunicationModel model, ClassFullName cfn) {
		this.model=model;
		this.cfn=cfn;
	}

	@Override
	public String generate() throws Exception {
		List<Class<?>> dtos= model.getDtos();
		write("package ");
		writeObject(cfn.getPackageName());
		write(";\npublic class ");
		writeObject(cfn.getSimpleName());
		write(" extends ");
		writeClass(SerializeBase.class);
		write("\n{\n\tpublic boolean serializeObject(Object value)\n\t{\n");
		for(Class<?> dot: dtos)
		{
			if(!SerializeBase.handles(dot))
			{
				write("\t\t\tif(value instanceof ");
				writeClass(dot);//NB
				write(")\n\t\t\t{\n\t\t\t\twriteString(\"");
				writeObject(dot.getName());
				write("\");\n");
				generateSerializationOfParams(dot);
				write("\t\t\t\treturn true;\n\t\t\t}\n");
			}
		}
		write("\t\treturn super.serializeObject(value);\n\t}\n\tpublic Object deserializeObject(String type)\n\t{\n\t\tswitch(type)\n\t\t{\n");
		for(Class<?> dot: dtos)
		{
			if(!SerializeBase.handles(dot))
			{
				write("\t\t\tcase \"");
				writeObject(dot.getName());
				write("\":\n\t\t\t{\n");
				if(dot.isEnum())
				{
					write("\t\t\t\treturn ");
					writeClass(dot);//NB
					write(".values()[readInt()];\n");
				}else
				{
					write("\t\t\t\t");
					writeClass(dot);//NB
					write(" ret=new ");
					writeClass(dot);//NB
					write("();\n");
					generateDeserializationOfParams(dot);
					write("\t\t\t\treturn ret;\n");
				}
				write("\t\t\t}\n");
			}
		}
		write("\t\t\tdefault:\n\t\t\t\treturn super.deserializeObject(type);\n\t\t}\n\t}\n}\n");
		return ""+getWriter();
	}
	public static Field[] getFieldsToSerialize(Class<?> dot)
	{
		List<Field> ret=new ArrayList<>();
		for(Field f: dot.getFields())
		{
			if(!Modifier.isStatic(f.getModifiers()))
			{
				ret.add(f);
			}
		}
		return ret.toArray(new Field[] {});
	}
	public static List<Pair<Method, Method>> getGetSettersToSerialize(Class<?> dot)
	{
		List<Pair<Method, Method>> ret=new ArrayList<>();
		for(Method m:dot.getMethods())
		{
			Method setter=getSetterPair(m);
			if(setter!=null)
			{
				ret.add(new Pair<Method, Method>(m, setter));
			}
		}
		return ret;
	}

	private void generateSerializationOfParams(Class<?> dot) throws Exception {
		if(dot.isEnum())
		{
			write("\t\t\t\twriteInt(((Enum<?>)value).ordinal());\n");
		}else
		{
			for(Field f: getFieldsToSerialize(dot))
			{
				write("\t\t\t\twriteString(\"");
				writeObject(f.getName());
				write("\");\n\t\t\t\tserializeObjectAssert(((");
				writeClass(dot);//NB
				write(")value).");
				writeObject(f.getName());
				write(");\n");
			}
			for(Pair<Method, Method> p: getGetSettersToSerialize(dot))
			{
				Method m=p.getA();
				write("\t\t\t\twriteString(\"");
				writeObject(m.getName());
				write("\");\n\t\t\t\tserializeObjectAssert(((");
				writeClass(dot);//NB
				write(")value).");
				writeObject(m.getName());
				write("());\n");
			}
			Class<?> supc=dot.getSuperclass();
			if(supc!=null)
			{
				generateSerializationOfParams(supc);
			}
		}
	}
	private void generateDeserializationOfParams(Class<?> dot) throws Exception {
		for(Field f: getFieldsToSerialize(dot))
		{
			write("\t\t\t\tassertEqual(\"");
			writeObject(f.getName());
			write("\", readString());\n\t\t\t\tret.");
			writeObject(f.getName());
			write("=(");
			writeClass(ProcessInterface.getWrapperClass(f.getType()));//NB
			write(") deserializeObject();\n");
		}
		for(Pair<Method, Method> p: getGetSettersToSerialize(dot))
		{
			Method m=p.getA();
			Method setter=p.getB();
			write("\t\t\t\tassertEqual(\"");
			writeObject(m.getName());
			write("\", readString());\n\t\t\t\tret.");
			writeObject(setter.getName());
			write("((");
			writeClass(ProcessInterface.getWrapperClass(setter.getParameters()[0].getType()));//NB
			write(") deserializeObject());\n");
		}
		Class<?> supc=dot.getSuperclass();
		if(supc!=null)
		{
			generateSerializationOfParams(supc);
		}
	}

	private static Method getSetterPair(Method m) {
		Class<?> dc=m.getDeclaringClass();
		String setterName=null;
		if(m.getName().startsWith("get"))
		{
			String simplename=m.getName().substring(3);
			setterName="set"+simplename;
		}
		if(m.getName().startsWith("is"))
		{
			String simplename=m.getName().substring(2);
			setterName="set"+simplename;
		}
		if(setterName!=null)
		{
			for(Method setterpair: dc.getMethods())
			{
				if(setterName.equals(setterpair.getName()) && setterpair.getParameterCount()==1)
				{
					return setterpair;
				}
			}
		}
		return null;
	}
}
