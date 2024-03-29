package hu.qgears.quickjs.serialization;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SerializeBase {
	private static Set<String> handledClasses=new HashSet<>();
	private ByteBufferOutput output=new ByteBufferOutput();
	private ByteBuffer input;
	static {
		handledClasses.add(Boolean.class.getName());
		handledClasses.add(Integer.class.getName());
		handledClasses.add(java.lang.Void.class.getName());
		handledClasses.add(java.lang.Character.class.getName());
		handledClasses.add(java.lang.Byte.class.getName());
		handledClasses.add(java.lang.Short.class.getName());
		handledClasses.add(java.lang.Long.class.getName());
		handledClasses.add(java.lang.Float.class.getName());
		handledClasses.add(java.lang.Double.class.getName());
		handledClasses.add(java.lang.Void.class.getName());
		handledClasses.add(String.class.getName());
		handledClasses.add(java.util.List.class.getName());
		handledClasses.add(java.util.ArrayList.class.getName());
		handledClasses.add(java.util.Set.class.getName());
		handledClasses.add(java.util.HashSet.class.getName());
		handledClasses.add(java.util.TreeSet.class.getName());
		handledClasses.add(java.util.Map.class.getName());
		handledClasses.add(java.util.TreeMap.class.getName());
		handledClasses.add(java.util.HashMap.class.getName());
	}
	/**
	 * 
	 * @param value
	 * @return false means not serialized because type is not known
	 */
	public boolean serializeObject(Object value)
	{
		if(value==null)
		{
			writeString("null");
			return true;
		}
		String claName=value.getClass().getName();
		if(value.getClass().isArray())
		{
			writeString("array");
			Class<?> componentType=getArrayType(value.getClass());
			writeString(componentType.getName());
			int l=Array.getLength(value);
			int d=getArrayDimension(value.getClass());
			writeInt(l);
			writeInt(d);
			for(int i=0;i<l;++i)
			{
				serializeObject(Array.get(value, i));
			}
			return true;
		}
		switch (claName) {
		case "java.lang.String":
			writeString(value.getClass().getName());
			writeString((String) value);
			return true;
		case "java.lang.Integer":
			writeString(value.getClass().getName());
			writeInt((Integer) value);
			return true;
		default:
			if (value instanceof List<?>)
			{
				List<?> l=(List<?>) value;
				writeString(ArrayList.class.getName());
				writeInt(l.size());
				for(int i=0;i<l.size();++i)
				{
					serializeObject(l.get(i));
				}
				return true;
			}
			return false;
		}
	}
	private int getArrayDimension(Class<?> cla) {
		if(cla.isArray())
		{
			return getArrayDimension(cla.getComponentType())+1;
		}
		return 0;
	}
	private Class<?> getArrayType(Class<?> cla) {
		if(cla.isArray())
		{
			return getArrayType(cla.getComponentType());
		}
		return cla;
	}
	protected void writeInt(int size) {
		output.writeInt(size);
	}

	protected void writeString(String s) {
		output.writeString(s);
	}
	protected void assertEqual(String string, String readString) {
		throw new RuntimeException("TODO");
	}
	protected String readString() {
		int l=input.getInt();
		StringBuilder ret=new StringBuilder();
		for(int i=0;i<l;++i)
		{
			short v=input.getShort();
			ret.append((char)v);
		}
		return ret.toString();
	}
	public static boolean handles(Class<?> dot) {
		if(handledClasses.contains(dot.getName()))
		{
			return true;
		}
		if(dot.isPrimitive())
		{
			return true;
		}
		return false;
	}
	public Object deserializeObject()
	{
		String type=readString();
		return deserializeObject(type);
	}
	public Object deserializeObject(String type) {
		switch(type)
		{
		case "java.lang.String":
			return readString();
		case "java.lang.Integer":
			return input.getInt();
		case "array":
		{
			String atype=readString();
			int l=input.getInt();
			int d=input.getInt();
			Object arr=createArray(atype, d, l);
			for(int i=0;i<l;++i)
			{
				Object o=deserializeObject();
				Array.set(arr, i, o);
			}
			return arr;
		}
		default:
			throw new RuntimeException("not known how to deserialize: "+type);
		}
	}
	private Object createArray(String atype, int d, int l) {
		Class<?> arrClass=getClass(atype);
		if(d<1 || d>255)
		{
			throw new IllegalArgumentException("Array dimension must be in range [1,255]: "+d);
		}
		while(d>1)
		{
			arrClass=Array.newInstance(arrClass, 0).getClass();
			d--;
		}
		return Array.newInstance(arrClass, l);
	}
	private Class<?> getClass(String claName) {
		switch(claName)
		{
		case "java.lang.Object":
			return Object.class;
		default:
			throw new RuntimeException("Can not create array of type: "+claName);
		}
	}
	public void reset() {
		output.reset();
	}
	public void setInput(ByteBuffer input) {
		this.input = input;
	}
	public ByteBuffer getSerializedBinary() {
		return output.getSerializedBinary();
	}
}
