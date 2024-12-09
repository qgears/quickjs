package hu.qgears.quickjs.qpage;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;

public class DisposableContainer implements IQDisposableContainer {
	private List<AutoCloseable> closeables;
	private static Logger log=LoggerFactory.getLogger(DisposableContainer.class);
	private volatile boolean disposed=false;
	public void close() {
		disposed=true;
		List<AutoCloseable> toClose;
		synchronized (this) {
			toClose=closeables;
			closeables=null;
		}
		if(toClose!=null)
		{
			for(AutoCloseable c: toClose)
			{
				closeCloseable(c);
			}
		}
	}
	private void closeCloseable(AutoCloseable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {
			log.error("Closing attached closeable", e);
		}
	}
	@Override
	public NoExceptionAutoClosable addCloseable(AutoCloseable closeable) {
		if(disposed)
		{
			closeCloseable(closeable);
			return new NoExceptionAutoClosable() {};
		}else
		{
			boolean secondCheck;
			synchronized (this) {
				secondCheck=disposed;
				if(!secondCheck)
				{
					if(closeables==null)
					{
						closeables=new ArrayList<>();
					}
					closeables.add(closeable);
				}
			}
			if(secondCheck)
			{
				closeCloseable(closeable);
				return new NoExceptionAutoClosable() {};
			}else
			{
				return new NoExceptionAutoClosable() {
					@Override
					public void close() {
						synchronized (DisposableContainer.this) {
							if(closeables!=null)
							{
								closeables.remove(closeable);
							}
						}
					}
				};
			}
		}
	}
	@Override
	public NoExceptionAutoClosable addOnClose(Runnable executeonclose) {
		return addCloseable(new NoExceptionAutoClosable() {
			@Override
			public void close() {
				executeonclose.run();
			}
		});
	}
}
