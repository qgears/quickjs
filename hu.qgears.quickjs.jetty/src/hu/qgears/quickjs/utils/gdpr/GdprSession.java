package hu.qgears.quickjs.utils.gdpr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import hu.qgears.quickjs.qpage.ISessionUpdateLastAccessedTime;
import hu.qgears.quickjs.qpage.jetty.QPageHandler;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

/**
 * GDPR compatible session object: only saved to Cookie once user accepts the cookie.
 */
@SuppressWarnings("deprecation")
public class GdprSession implements HttpSession, ISessionUpdateLastAccessedTime {

	private Map<String, Object> attributes=new HashMap<>();
	private long creationTime;
	private long lastAccessedTime;
	private String id;
	private int maxInactiveInterval;
	private volatile String userAgent;
	public static String keyUseNoCookieSession="hu.qgears.quickjs.utils.gdpr.SimpleSession.useNoCookieSession";
	public static final String keySessionIdParameterName=QPageHandler.class.getName()+".sessionId";
	private GdprSessionIdManager host;
	protected Long currentTimeoutRegistered=null;
	protected boolean cookiesAccepted=false;
	
	public GdprSession(GdprSessionIdManager host, long creationTime, String id) {
		super();
		this.host=host;
		this.creationTime = creationTime;
		this.id=id;
		this.lastAccessedTime=creationTime;
	}

	@Override
	public Object getAttribute(String id) {
		synchronized (attributes) {
			return attributes.get(id);
		}
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		synchronized (attributes) {
			return Collections.enumeration(new ArrayList<>(attributes.keySet()));
		}
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}
	private boolean isNew;
	private ServletContext servletContext;
	private HttpSessionContext sessionContext;
	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return sessionContext;
	}

	@Override
	public Object getValue(String id) {
		synchronized (attributes) {
			return attributes.get(id);
		}
	}

	@Override
	public String[] getValueNames() {
		synchronized (attributes) {
			Set<String> s=attributes.keySet();
			String[] ret=new String[s.size()];
			int i=0;
			for(String st:s)
			{
				ret[i]=st;
				i++;
			}
			return ret;
		}
	}

	@Override
	public void invalidate() {
		host.dispose(this, false, 0);
	}
	@Override
	public boolean isNew() {
		return isNew;
	}

	@Override
	public void putValue(String id, Object value) {
		synchronized (attributes) {
			attributes.put(id, value);
		}
	}

	@Override
	public void removeAttribute(String id) {
		synchronized (attributes) {
			attributes.remove(id);
		}
	}

	@Override
	public void removeValue(String id) {
		synchronized (attributes) {
			attributes.remove(id);
		}
	}

	@Override
	public void setAttribute(String id, Object value) {
		synchronized (attributes) {
			attributes.put(id, value);
		}
	}
	@Override
	public void setMaxInactiveInterval(int arg0) {
		this.maxInactiveInterval=arg0;
	}

	protected void updateLastAccessedTime(long t) {
		this.lastAccessedTime=t;
	}

	public long getRequiredDisposeTimeAt() {
		return lastAccessedTime+maxInactiveInterval*1000;
	}
	public boolean areCookiesAccepted()
	{
		return cookiesAccepted;
	}
	public String getAcceptCookiesCookieName() {
		return host.getAcceptCookiesCookieName();
	}
	public String getSessionIdCookieName()
	{
		return host.getSessionIdCookieName();
	}
	@Override
	public void setLastAccessedTime(long t) {
		lastAccessedTime=t;
		host.updateTimeout(this);
	}
	public void setCookieAccepted() {
		host.setCookieAccepted(this);
	}
	public void setUserAgent(String userAgent) {
		this.userAgent=userAgent;
	}

	public String getUserAgent() {
		return userAgent;
	}
}
