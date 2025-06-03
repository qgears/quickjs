package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.helpers.QTimer;

/** A page of an SPA application - the content of the <body>
 * Exactly single instance must exist all time below the QPageContainer object.
 **/
public class QPage extends QComponent {
	/**
	 * The current page handled on this thread.
	 */
	private static ThreadLocal<QPage> currentPage=new ThreadLocal<>();
	public QPage(QPageContainer parent)
	{
		super(parent);
		QPage prev=currentPage.get();
		currentPage.set(this);
		try
		{
			init();
		}finally
		{
			currentPage.set(prev);
		}
	}
	@Override
	protected void doInitJSObject() {
		write("\tnew QPage(page, \"");
		writeObject(id);
		write("\");\n");
	}
	@Override
	public void handle(JSONObject post) throws IOException {
		// Nothing to do
	}
	/**
	 * Set this QPage object to be the current for this thread.
	 * @return
	 */
	public static NoExceptionAutoClosable setThreadCurrentPage(QPage page) {
		QPage prev=currentPage.get();
		currentPage.set(page);
		return new NoExceptionAutoClosable() {
			@Override
			public void close() {
				currentPage.set(prev);
			}
		};
	}
	public static QPage getCurrent() {
		return currentPage.get();
	}
	@Override
	public QPageContainer getParent() {
		return (QPageContainer)super.getParent();
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
