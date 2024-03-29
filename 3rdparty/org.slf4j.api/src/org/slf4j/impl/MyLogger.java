package org.slf4j.impl;

import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class MyLogger implements Logger {
	private String name;
	private int level=Level.INFO.intValue();
	
	public MyLogger(String name) {
		super();
		this.name = name;
	}
	public void setLevel(Level level)
	{
		this.level=level.intValue();
	}
	@Override
	public boolean isWarnEnabled(Marker marker) {
		return isWarnEnabled();
	}

	@Override
	public boolean isDebugEnabled() {
		return level<=Level.FINE.intValue();
	}
	@Override
	public boolean isInfoEnabled() {
		return level<=Level.INFO.intValue();
	}
	@Override
	public boolean isTraceEnabled() {
		return level<=Level.FINEST.intValue();
	}
	@Override
	public boolean isTraceEnabled(Marker marker) {
		return isTraceEnabled();
	}
	@Override
	public boolean isDebugEnabled(Marker marker) {
		return isDebugEnabled();
	}

	
	@Override
	public boolean isInfoEnabled(Marker marker) {
		return isInfoEnabled();
	}


	@Override
	public boolean isWarnEnabled() {
		return level<=Level.WARNING.intValue();
	}
	@Override
	public boolean isErrorEnabled(Marker marker) {
		return isErrorEnabled();
	}
	@Override
	public boolean isErrorEnabled() {
		return level<=Level.SEVERE.intValue();
	}



	@Override
	public String getName() {
		return name;
	}


	@Override
	public void trace(String msg) {
		fmt(Level.FINEST, msg);
	}

	@Override
	public void trace(String format, Object arg) {
		fmt(Level.FINEST, format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		fmt(Level.FINEST, format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object... arguments) {
		fmt(Level.FINEST, format, arguments);
	}

	@Override
	public void trace(String msg, Throwable t) {
		fmt(Level.FINEST, msg, t);
	}
	@Override
	public void trace(Marker marker, String msg) {
		fmt(Level.FINEST, marker, msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		fmt(Level.FINEST, marker, format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		fmt(Level.FINEST, marker, format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {
		fmt(Level.FINEST, marker, format, argArray);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		fmt(Level.FINEST, marker, msg, t);
	}
	
	@Override
	public void debug(String msg) {
		fmt(Level.FINE, msg);
	}

	@Override
	public void debug(String format, Object arg) {
		fmt(Level.FINE, format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		fmt(Level.FINE, format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object... arguments) {
		fmt(Level.FINE, format, arguments);
	}

	@Override
	public void debug(String msg, Throwable t) {
		fmt(Level.FINE, msg, t);
	}
	@Override
	public void debug(Marker marker, String msg) {
		fmt(Level.FINE, marker, msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		fmt(Level.FINE, marker, format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		fmt(Level.FINE, marker, format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object... argArray) {
		fmt(Level.FINE, marker, format, argArray);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		fmt(Level.FINE, marker, msg, t);
	}


	@Override
	public void warn(String msg) {
		fmt(Level.WARNING, msg);
	}

	@Override
	public void warn(String format, Object arg) {
		fmt(Level.WARNING, format, arg);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		fmt(Level.WARNING, format, arg1, arg2);
	}

	@Override
	public void warn(String format, Object... arguments) {
		fmt(Level.WARNING, format, arguments);
	}

	@Override
	public void warn(String msg, Throwable t) {
		fmt(Level.WARNING, msg, t);
	}
	@Override
	public void warn(Marker marker, String msg) {
		fmt(Level.WARNING, marker, msg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		fmt(Level.WARNING, marker, format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		fmt(Level.WARNING, marker, format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... argArray) {
		fmt(Level.WARNING, marker, format, argArray);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		fmt(Level.FINEST, marker, msg, t);
	}

	@Override
	public void error(String msg) {
		fmt(Level.SEVERE, msg);
	}

	@Override
	public void error(String format, Object arg) {
		fmt(Level.SEVERE, format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		fmt(Level.SEVERE, format, arg1, arg2);
	}

	@Override
	public void error(String format, Object... arguments) {
		fmt(Level.SEVERE, format, arguments);
	}


	@Override
	public void error(String msg, Throwable t) {
		fmt(Level.SEVERE, msg, t);
	}


	@Override
	public void error(Marker marker, String msg) {
		fmt(Level.SEVERE, marker, msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		fmt(Level.SEVERE, marker, format, arg);
	}


	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		fmt(Level.SEVERE, marker, format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {
		fmt(Level.SEVERE, marker, format, arguments);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		fmt(Level.SEVERE, marker, msg, t);
	}

	private void fmt(Level severe, String msg) {
		System.out.println(""+severe+" "+msg);
//		OLog.fmt(severe, ""+name+": "+msg);
	}
	
	private void fmt(Level severe, String format, Object... arguments) {
		if(severe.intValue()>=level)
		{
			System.out.println(""+severe+" "+format+" "+arguments);
//			OLog.fmt(severe, ""+name+": "+format, arguments);
		}
	}
	private void fmt(Level severe, String msg, Throwable t) {
		if(severe.intValue()>=level)
		{
			System.out.println(""+severe+" "+msg+" "+t);
//			OLog.fmt(severe, ""+name+": "+msg, t);
		}
	}
	private void fmt(Level severe, Marker m, String msg, Throwable t) {
		if(severe.intValue()>=level)
		{
			System.out.println(""+severe+" "+m+" "+msg+" "+t);
//			OLog.fmt(severe, m, ""+name+": "+msg, t);
		}
	}
	
	private void fmt(Level severe, Marker marker, String msg) {
		if(severe.intValue()>=level)
		{
			System.out.println(""+severe+" "+marker+" "+msg);
//			OLog.fmt(severe, marker, ""+name+": "+msg);
		}
	}
	private void fmt(Level severe, Marker marker, String format, Object... arg) {
		System.err.println(""+severe+": "+String.format(format, arg));
	}


	@Override
	public void info(String msg) {
		fmt(Level.INFO, msg);
	}

	@Override
	public void info(String format, Object arg) {
		fmt(Level.INFO, format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		fmt(Level.INFO, format, arg1, arg2);
	}

	@Override
	public void info(String format, Object... arguments) {
		fmt(Level.INFO, format, arguments);
	}

	@Override
	public void info(String msg, Throwable t) {
		fmt(Level.INFO, msg, t);
	}
	@Override
	public void info(Marker marker, String msg) {
		fmt(Level.INFO, marker, msg);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		fmt(Level.INFO, marker, format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		fmt(Level.INFO, marker, format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... argArray) {
		fmt(Level.INFO, marker, format, argArray);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		fmt(Level.INFO, marker, msg, t);
	}
}
