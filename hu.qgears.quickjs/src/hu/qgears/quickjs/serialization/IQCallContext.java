package hu.qgears.quickjs.serialization;

/** Access to web server context information of the current query (QPage or Web socket): session, user, etc.
 * In case of the remoting generator marks that the value is not received from client but is the (HTTP) context (session, user, etc).
 * The interface is only used on the SERVER side.
 * Implementation is technology specific.
 */
public interface IQCallContext {
	/** Cookie name that stores when storing cookies was accepted by the user. 
	 * @return
	 */
	String getAcceptCookiesCookieName();
	/** Name of the cookie that stores the session identifier. Must be set in the browser when the cookies are accepted by the user. 
	 * @return
	 */
	String getSessionIdCookieName();
	/** Identifier of the session. Must be stored into getSessionIdCookieName.
	 * @return
	 */
	String getSessionId();
	/** Set session id known by the client.
	 * @param sessionId
	 */
	void setSessionId(String sessionId);
	/** Is cookie already accepted? */
	boolean isCookieAccepted();
	Object getSession();
}
