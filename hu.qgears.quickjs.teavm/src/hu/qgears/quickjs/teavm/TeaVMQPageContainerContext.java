package hu.qgears.quickjs.teavm;

import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.qpage.IQPageContaierContext;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.serialization.ClientSideCallContextData;

public class TeaVMQPageContainerContext implements IQPageContaierContext {
	public final QPage page;
	public Object initializeObject;
	public Object remoting;
	private ClientSideCallContextData data;
	private Object remote;
	
	/**
	 * 
	 * @param page
	 * @param data
	 * @param remote project specific interface to access the server
	 */
	public TeaVMQPageContainerContext(QPage page, ClientSideCallContextData data, Object remote) {
		super();
		this.page = page;
		this.data = data;
		this.remote=remote;
	}

	@Override
	public Object getInitializeObject() {
		return initializeObject;
	}

	@Override
	public Object getRemoting() {
		return remoting;
	}

	@Override
	public String getPageContextPath() {
		return data.pageContextPath;
	}

	@Override
	public Promise<String> getResourcePath(String resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResourcePathSync(String resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QPage getPage() {
		return page;
	}

	@Override
	public String getImagePathSync(String s) {
		return getResourcePathSync(s);
	}

	@Override
	public String getImagePathSync(String s, int thumbNailXSize) {
		return getResourcePathSync(s);
	}

	@Override
	public String getAcceptCookiesCookieName() {
		return data.acceptCookiesCookieName;
	}

	@Override
	public String getSessionIdCookieName() {
		return data.sessionIdCookieName;
	}

	@Override
	public String getSessionId() {
		return data.sessionId;
	}

	@Override
	public boolean isCookieAccepted() {
		return data.cookieAccepted;
	}

	@Override
	public void setSessionId(String sessionId) {
		data.sessionId=sessionId;
	}

	@Override
	public Object getSession() {
		throw new RuntimeException();
	}
}
