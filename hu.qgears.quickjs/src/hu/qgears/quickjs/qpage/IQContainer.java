package hu.qgears.quickjs.qpage;

import hu.qgears.commons.NoExceptionAutoClosable;

/** Interface of nodes that can contain other nodes. */
public interface IQContainer {
	/** Get reference to the hosting page container. */
	QPageContainer getPageContainer();
	/** Get reference to the parent container. null in case of the QPageContainer or if the object was removed from the tree. */
	IQContainer getParent();
	/** Get the identifier of this object. */
	String getId();
	/**
	 * Add a closeable object to this component: when this component is disposed then this closeable will also be closed.
	 * If this object is already disposed then the closeable object is closed at once.
	 * After disposing the reference will be dropped.
	 * Exceptions of closing are caught and logged if they are raised.
	 * @param closeable
	 * @return when closed then the closeable is removed from the internal list. Useful when autoclosable objects are created and disposed regularly like timers to avoid leak.
	 */
	NoExceptionAutoClosable addCloseable(AutoCloseable closeable);
	/**
	 * Not to be used by user: library auto-registers child objects through this API.
	 * @param child
	 */
	void addChild(QComponent child);
	/**
	 * Not to be used by user: library auto-de-registers child objects through this API.
	 * @param child
	 */
	void removeChild(QComponent child);
}
