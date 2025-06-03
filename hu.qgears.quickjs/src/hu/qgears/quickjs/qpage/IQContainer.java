package hu.qgears.quickjs.qpage;

/** Interface of nodes that can contain other nodes. */
public interface IQContainer extends IQDisposableContainer {
	/** Get reference to the hosting page container. */
	QPageContainer getPageContainer();
	/** Get reference to the parent container. null in case of the QPageContainer or if the object was removed from the tree. */
	IQContainer getParent();
	/** Get the identifier of this object. */
	String getId();
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
