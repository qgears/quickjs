package hu.qgears.quickjs.serialization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SerializeBase {
	private static Set<String> handledClasses=new HashSet<>();
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
	protected boolean serializeObject(Object value)
	{
		if(value==null)
		{
			writeString("null");
			return true;
		}
		switch (value.getClass().getName()) {
		case "java.lang.String":
			writeString(value.getClass().getName());
			writeString((String) value);
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

	private void writeInt(int size) {
		throw new RuntimeException("TODO");
	}

	private void writeString(String s) {
		throw new RuntimeException("TODO");
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
}
