package hu.qgears.quickjs.qpage;

import hu.qgears.quickjs.serialization.RemotingBase;

/** Access the context of the QPageContainer.
 * This object is accessible both on the server(JVM) and the client(TeaVM).
 * For this reason its methods are serializable and remote callable.
 * {@link hu.qgears.quickjs.serialization.ProcessInterface} generator can be used to generate the necessary serialization and remoting classes
 */
public interface IQPageContaierContext {
	/** Object created by the embedding code on the server side. It is initialized
	 * when the first get query is handled by the https server and its type is application specific.
	 * In case of client side execution this object is passed to the client in serialized format.
	 * @return
	 */
	Object getInitializeObject();
	/** Open the server connection with the given id.
	 * Each QPageContainer has a pre-defined set of id->Remote server interface mappings and this method opens
	 * one of those connections.
	 *  * On the server side execution is synchronized
	 *  * on the client side execution is async
	 * The usable ids and the type of the mapped server interfaces are defined by the application.
	 * @param id
	 * @return
	 */
	RemotingBase openConnection(String id);
	/** Get the rewrite context of this page (if a reverse proxy rewrite is present)
	 * @param baseRequest
	 * @return never ends with / Will be '' if no reverse proxy is present. 'path' when reverse proxy is present
	 */
	String getPageContextPath();
}
