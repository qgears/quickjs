package hu.qgears.quickjs.qpage.example;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.utils.IQTestEnvironment;

/**
 * Object required to instantiate a QPageHandler.
 */
public class QPageContext {
	public final Server server;
	private IQTestEnvironment testEnvironment;
	private String sessionIdParameter;
	private Map<String, Object> userObjects=new HashMap<>();
	private static ThreadLocal<QPageContext> current=new ThreadLocal<>();

	public QPageContext(Server server) {
		super();
		this.server = server;
		if(server==null)
		{
			throw new NullPointerException();
		}
	}
	public void setSessionIdParameter(String sessionIdParameter) {
		this.sessionIdParameter = sessionIdParameter;
	}
	public String getSessionIdParameter() {
		return sessionIdParameter;
	}
	public static QPageContext getCurrent() {
		return current.get();
	}
	public static void setCurrent(QPageContext current) {
		QPageContext.current.set(current);
	}
	public NoExceptionAutoClosable setupCurrent() {
		QPageContext prev=current.get();
		QPageContext.current.set(this);
		return new NoExceptionAutoClosable() {
			@Override
			public void close() {
				current.set(prev);
			}
		};
	}
	public Object getUserObject(String key) {
		synchronized (userObjects) {
			return userObjects.get(key);
		}
	}
	/**
	 * Interface to communicate with test environment when auto-testing is used.
	 * Should be null in production.
	 * @param key
	 * @param userObject
	 */
	public void setUserObject(String key, Object userObject) {
		synchronized (userObjects) {
			this.userObjects.put(key, userObject);
		}
	}
	public void setTestEnvironment(IQTestEnvironment testEnvironment) {
		this.testEnvironment = testEnvironment;
	}
	public IQTestEnvironment getTestEnvironment() {
		return testEnvironment;
	}
}
