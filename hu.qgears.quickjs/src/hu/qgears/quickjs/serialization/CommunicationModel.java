package hu.qgears.quickjs.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommunicationModel {
	/**
	 * These classes are auto-remoted
	 */
	public Set<Class<?>> serverInterfaces=new HashSet<>();
	
	/**
	 * These classes are serialized/deserialized
	 */
	public Set<Class<?>> dtos=new HashSet<>();
	
	public List<Class<?>> getDtos()
	{
		List<Class<?>> ret=new ArrayList<>(dtos);
		Collections.sort(ret, new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return ret;
	}
}
