package hu.qgears.quickjs.qpage;

public interface IQContainer {
	QPageContainer getPage();
	IQContainer getParent();
	String getId();
	/**
	 * Add a closeable object to this component: when this component is disposed then this closeable will also be closed.
	 * If this object is already disposed then the closeable object is closed at once.
	 * After disposing the reference will be dropped.
	 * Exceptions of closing are caught and logged if they are raised.
	 * @param closeable
	 */
	void addCloseable(AutoCloseable closeable);
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
