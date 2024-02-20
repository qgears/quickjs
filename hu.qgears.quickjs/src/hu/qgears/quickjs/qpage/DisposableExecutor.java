package hu.qgears.quickjs.qpage;

import java.io.Closeable;
import java.util.concurrent.Executor;

public class DisposableExecutor implements Closeable
{
	private Executor executor;
	private volatile boolean closed=false;
	
	public DisposableExecutor(Executor executor) {
		super();
		this.executor = executor;
	}
	public Executor getExecutor()
	{
		return executor;
	}
	@Override
	final public void close() {
		closed=true;
		doClose();
	}
	protected void doClose() {
	}
	public final boolean isClosed()
	{
		return closed;
	}
}
