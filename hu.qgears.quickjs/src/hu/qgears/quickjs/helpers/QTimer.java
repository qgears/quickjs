package hu.qgears.quickjs.helpers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;

abstract public class QTimer implements NoExceptionAutoClosable {
	private static final Logger log=LoggerFactory.getLogger(QTimer.class);
	private volatile boolean isClosed;
	private List<NoExceptionAutoClosable> l=new ArrayList<>(1);
	public void addCloseable(NoExceptionAutoClosable ac)
	{
		if(l!=null)
		{
			l.add(ac);
		}
	}
	@Override
	public void close() {
		isClosed=true;
		NoExceptionAutoClosable.super.close();
		try {
			for(NoExceptionAutoClosable c: l)
			{
				c.close();
			}
			l=null;
		} catch (Exception e) {
			log.error("Removing listeners",e);
		}
	}
	public boolean isClosed() {
		return isClosed;
	}
}
