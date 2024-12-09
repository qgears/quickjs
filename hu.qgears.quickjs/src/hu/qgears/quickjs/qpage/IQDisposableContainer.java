package hu.qgears.quickjs.qpage;

import hu.qgears.commons.NoExceptionAutoClosable;

public interface IQDisposableContainer {
	/**
	 * Add a closeable object to this component: when this component is disposed then this closeable will also be closed.
	 * If this object is already disposed then the closeable object is closed at once.
	 * After disposing the reference will be dropped.
	 * Exceptions of closing are caught and logged if they are raised.
	 * @param closeable
	 * @return when closed then the closeable is removed from the internal list. Useful when autoclosable objects are created and disposed regularly like timers to avoid leak.
	 */
	NoExceptionAutoClosable addCloseable(AutoCloseable closeable);
	public NoExceptionAutoClosable addOnClose(Runnable executeonclose);
}
