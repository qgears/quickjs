package hu.qgears.quickjs.serverif;

import hu.qgears.quickjs.serialization.IRemotingBase;

/** Interface to communicate to the server - basic features. */
public interface IQuickJsServerIfRemote extends IRemotingBase {
	/** Signal that cookies are accepted for the current session so the session has to be transformed from short living to long living.
	 * @return the session id that may be different that original if a new session was created in the meantime. */
	String signalCookieAcceptedForSession();
}
