package hu.qgears.quickjs.qpage;

/**
 * Callback interface towards the web server to signal that the session
 * has to be kept alive. Useful in cases when the session is designed to be short lived
 * but an active QPage keeps the session object alive.
 * (The default session implementation keeps the session alive when a http query is initiated but an open websocket
 * connection does not keep the session alive.)
 */
public interface ISessionUpdateLastAccessedTime {
	/**
	 * Set the last access time of the session.
	 * Useful in case of short live session (cookies not accepted) to extend its life span while the QPage is open.
	 * @param t System.currentTimeMillis() format timestamp of now
	 */
	void setLastAccessedTime(long t);
}
