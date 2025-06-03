package hu.qgears.quickjs.qpage;

import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.serialization.ClientSideCallContextData;
import hu.qgears.quickjs.serialization.IQCallContext;

/** Access the context of the QPageContainer.
 * This object is accessible both on the server(JVM) and the client(TeaVM).
 * For this reason its methods are serializable and remote callable.
 * {@link hu.qgears.quickjs.serialization.generator.ProcessInterface} generator can be used to generate the necessary serialization and remoting classes
 */
public interface IQPageContaierContext extends IQCallContext {
	/** Object created by the embedding code on the server side. It is initialized
	 * when the first get query is handled by the https server and its type is application specific.
	 * In case of client side execution this object is passed to the client in serialized format.
	 * @return
	 */
	Object getInitializeObject();
	/** Get the server remoting root object.
	 * Each QPageContainer has a pre-defined interface that can be used to get server interfaces.
	 *  * On the server side execution is synchronized
	 *  * on the client side execution is async
	 * @return
	 */
	Object getRemoting();
	/** Get the rewrite context of this page (if a reverse proxy rewrite is present)
	 * @param baseRequest
	 * @return never ends with / Will be '' if no reverse proxy is present. 'path' when reverse proxy is present
	 */
	String getPageContextPath();
	/** Convert a fixed resource path (typically written into code directly) to a resource path that can be put into DOM tree (script.rel, img.src, etc.)
	 * Implementation is application specific. A basic implementation would pre-append the pageContextPath
	 * and add cache specific features to the path. For example append ?hash=sha1sum prevents that a cached version of the
	 * file is stuck in the DOM tree.
	 * Excution of the call is asynchronous and will return a promise. The current sha1sum may be resolved on the server for example.
	 * @param resourceId
	 * @return
	 */
	Promise<String> getResourcePath(String resourceId);
	/** Convert a fixed resource path (typically written into code directly) to a resource path that can be put into DOM tree (script.rel, img.src, etc.)
	 * Implementation is application specific. A basic implementation would pre-append the pageContextPath
	 * and add cache specific features to the path. For example append ?hash=sha1sum prevents that a cached version of the
	 * file is stuck in the DOM tree.
	 * Excution of the call is synchronous and will throw an exception in case synchronous execution is not possible.
	 * 
	 * Should be used for example for the initial resources: CSS, JS files, images that are loaded with link rel=preload.
	 * @param resourceId
	 * @return
	 */
	String getResourcePathSync(String resourceId);
	/** Get reference to the current active page.
	 * @return
	 */
	QPage getPage();
	String getImagePathSync(String s);
	String getImagePathSync(String s, int thumbNailXSize);
	default ClientSideCallContextData createClientSideCallContext()
	{
		ClientSideCallContextData ret=new ClientSideCallContextData();
		ret.acceptCookiesCookieName=getAcceptCookiesCookieName();
		ret.cookieAccepted=isCookieAccepted();
		ret.initObject=getInitializeObject();
		ret.pageContextPath=getPageContextPath();
		ret.sessionId=getSessionId();
		ret.sessionIdCookieName=getSessionIdCookieName();
		return ret;
	}
}
