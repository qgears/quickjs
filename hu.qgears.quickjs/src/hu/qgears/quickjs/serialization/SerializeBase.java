package hu.qgears.quickjs.serialization;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
		handledClasses.add(CallbackRegistryEntry.class.getName());
		handledClasses.add(RemoteMessageObject.class.getName());
		handledClasses.add(ClientSideCallContextData.class.getName());
	}
	public void serializeObjectAssert(Object value)
	{
		boolean handled=serializeObject(value);
		if(!handled)
		{
			throw new RuntimeException("Serialization of type not handled: "+value+" "+value.getClass());
		}
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
				serializeObjectAssert(Array.get(value, i));
			}
			return true;
		}
		if(value instanceof CallbackRegistryEntry)
		{
			CallbackRegistryEntry cre=(CallbackRegistryEntry) value;
			writeString("callback");
			writeInt(cre.id);
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
		case "java.lang.Boolean":
			writeString(value.getClass().getName());
			output.writeBool((Boolean) value);
			return true;
		case "java.lang.Long":
			writeString(value.getClass().getName());
			writeLong((Long) value);
			return true;
		case "hu.qgears.quickjs.serialization.RemoteMessageObject":
			writeString(value.getClass().getName());
			RemoteMessageObject ro=(RemoteMessageObject) value;
			writeInt(ro.type);
			writeInt(ro.callbackId);
			writeString(ro.iface);
			writeString(ro.methodPrototype);
			serializeObjectAssert(ro.argumentsToSerialize);
			serializeObjectAssert(ro.ret);
			return true;
		case "hu.qgears.quickjs.serialization.ClientSideCallContextData":
		{
			writeString(value.getClass().getName());
			ClientSideCallContextData d=(ClientSideCallContextData) value;
			writeString(d.acceptCookiesCookieName);
			output.writeBool(d.cookieAccepted);
			writeString(d.pageContextPath);
			writeString(d.sessionId);
			writeString(d.sessionIdCookieName);
			serializeObjectAssert(d.initObject);
			return true;
		}
		default:
			if (value instanceof List<?>)
			{
				List<?> l=(List<?>) value;
				writeString(ArrayList.class.getName());
				writeInt(l.size());
				for(int i=0;i<l.size();++i)
				{
					serializeObjectAssert(l.get(i));
				}
				return true;
			}
			if (value instanceof Map<?, ?>)
			{
				Map<?,?> l=(Map<?,?>) value;
				if(value instanceof SortedMap)
				{
					writeString(TreeMap.class.getName());
				}else
				{
					writeString(HashMap.class.getName());
				}
				writeInt(l.size());
				for(Map.Entry<?, ?> entry: l.entrySet())
				{
					serializeObjectAssert(entry.getKey());
					serializeObjectAssert(entry.getValue());
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
	protected void writeLong(long l) {
		output.writeLong(l);
	}

	protected void writeString(String s) {
		output.writeString(s);
	}
	protected void assertEqual(String string, String readString) {
		if(!string.equals(readString))
		{
			throw new RuntimeException("assert error: "+string+"!="+readString);
		}
	}
	protected int readInt()
	{
		return input.getInt();
	}
	protected String readString() {
		int l=input.getInt();
		if(l==-1)
		{
			return null;
		}
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
	@SuppressWarnings("unchecked")
	public Object deserializeObject(String type) {
		switch(type)
		{
		case "java.lang.String":
			return readString();
		case "java.lang.Integer":
			return input.getInt();
		case "java.lang.Boolean":
			return readBool();
		case "java.lang.Long":
			return input.getLong();
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
		case "callback":
		{
			int index=input.getInt();
			@SuppressWarnings("rawtypes")
			CallbackOnRemote rem=new CallbackOnRemote(index);
			return rem;
		}
		case "hu.qgears.quickjs.serialization.RemoteMessageObject":
		{
			RemoteMessageObject ro=new RemoteMessageObject();
			ro.type=input.getInt();
			ro.callbackId=input.getInt();
			ro.iface=readString();
			ro.methodPrototype=readString();
			ro.argumentsToSerialize=(Object[])deserializeObject();
			ro.ret=deserializeObject();
			return ro;
		}
		case "hu.qgears.quickjs.serialization.ClientSideCallContextData":
		{
			ClientSideCallContextData ret=new ClientSideCallContextData();
			ret.acceptCookiesCookieName=readString();
			ret.cookieAccepted=readBool();
			ret.pageContextPath=readString();
			ret.sessionId=readString();
			ret.sessionIdCookieName=readString();
			ret.initObject=deserializeObject();
			return ret;
		}
		case "null":
		{
			return null;
		}
		case "java.util.ArrayList":
		{
			@SuppressWarnings("rawtypes")
			List ret=new ArrayList();
			int l=input.getInt();
			for(int i=0;i<l;++i)
			{
				Object value=deserializeObject();
				ret.add(value);
			}
			return ret;
		}
		case "java.util.TreeMap":
		{
			@SuppressWarnings("rawtypes")
			TreeMap ret=new TreeMap();
			int l=input.getInt();
			for(int i=0;i<l;++i)
			{
				Object key=deserializeObject();
				Object value=deserializeObject();
				ret.put(key, value);
			}
			return ret;
		}
		case "java.util.HashMap":
		{
			@SuppressWarnings("rawtypes")
			HashMap ret=new HashMap();
			int l=input.getInt();
			for(int i=0;i<l;++i)
			{
				Object key=deserializeObject();
				Object value=deserializeObject();
				ret.put(key, value);
			}
			return ret;
		}
		default:
			throw new RuntimeException("not known how to deserialize: '"+type+"'");
		}
	}
	private boolean readBool() {
		byte v=input.get();
		return v!=0;
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
		case "java.lang.String":
			return String.class;
		case "java.lang.Integer":
			return Integer.class;
		case "java.lang.Long":
			return Long.class;
		case "int":
			return int.class;
		case "long":
			return long.class;
		default:
			throw new RuntimeException("Can not create array of type: "+claName);
		}
	}
	public void reset() {
		output.reset();
	}
	public void setInput(ByteBuffer input) {
		this.input = input;
		input.order(ByteOrder.LITTLE_ENDIAN);
	}
	public ByteBuffer getSerializedBinary() {
		return output.getSerializedBinary();
	}
	public ByteBufferOutput getOutput() {
		return output;
	}
}
