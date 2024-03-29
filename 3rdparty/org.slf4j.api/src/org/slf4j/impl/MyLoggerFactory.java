package org.slf4j.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class MyLoggerFactory implements ILoggerFactory {
	private Map<String, Logger> cache=new HashMap<>();
	@Override
	public Logger getLogger(String name) {
		synchronized (this) {
			Logger ret=cache.get(name);
			if(ret==null)
			{
				ret=new MyLogger(name);
				cache.put(name, ret);
			}
			return ret;
		}
	}
}
