package hu.qgears.quickjs.serverside;

import hu.qgears.quickjs.serialization.IQCallContext;
import hu.qgears.quickjs.utils.gdpr.GdprSession;

public class QCallContext implements IQCallContext {
	private GdprSession session;
	
	@Override
	public String getAcceptCookiesCookieName() {
		return session.getAcceptCookiesCookieName();
	}
	@Override
	public String getSessionIdCookieName() {
		return session.getSessionIdCookieName();
	}
	@Override
	public String getSessionId() {
		return session.getId();
	}
	@Override
	public boolean isCookieAccepted() {
		return session.areCookiesAccepted();
	}
	public void setCookieAccepted(boolean cookieAccepted) {
		if(cookieAccepted)
		{
			session.setCookieAccepted();
		}
	}
	public void setSession(GdprSession session) {
		this.session = session;
	}
	@Override
	public void setSessionId(String sessionId) {
		// In case of server side implementation the sessionid can not change during the lifetime of a single context.
	}
	public GdprSession getSession() {
		return session;
	}
}
