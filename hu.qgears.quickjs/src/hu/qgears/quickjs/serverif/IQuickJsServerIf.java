package hu.qgears.quickjs.serverif;

public interface IQuickJsServerIf {
	/** Signal that cookies are accepted for the current session so the session has to be transformed from short living to long living.
	 * @return the session id that may be different that original if a new session was created in the meantime. */
	String signalCookieAcceptedForSession();
}
