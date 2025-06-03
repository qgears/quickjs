package hu.qgears.quickjs.serialization;

/** No-op implementation of the interface for test purpose. */
public class QCallContextMock implements IQCallContext {
	@Override
	public String getAcceptCookiesCookieName() {
		throw new RuntimeException("not implemented");
	}
	@Override
	public String getSessionIdCookieName() {
		throw new RuntimeException("not implemented");
	}
	@Override
	public String getSessionId() {
		throw new RuntimeException("not implemented");
	}
	@Override
	public boolean isCookieAccepted() {
		throw new RuntimeException("not implemented");
	}
	@Override
	public void setSessionId(String sessionId) {
		throw new RuntimeException("not implemented");
	}
	@Override
	public Object getSession() {
		throw new RuntimeException("not implemented");
	}
}
