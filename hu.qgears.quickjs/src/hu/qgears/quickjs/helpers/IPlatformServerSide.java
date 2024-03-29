package hu.qgears.quickjs.helpers;

import hu.qgears.quickjs.qpage.IndexedComm;
import hu.qgears.quickjs.qpage.QPageManager;

public interface IPlatformServerSide extends IPlatform {
	/**
	 * Get the communication object that connects to the JS program.
	 * @return
	 */
	IndexedComm getIndexedComm();

	/**
	 * Get the QPageManager that registers this page.
	 * @return
	 */
	QPageManager getQPageManager();

	/**
	 * In case of a custom Websocket connection is used by a QPage component (file upload for example)
	 * then return the object for the identifier.
	 * TODO this feature is not independent of SERVER vs client layout. Must be redesigned
	 * @param string
	 * @return
	 */
	IndexedComm getCustomWebsocketImplementation(String string);

	/**
	 * TODO this feature is not independent of SERVER vs client layout. Must be redesigned
	 * @param comm
	 * @return
	 */
	String registerCustomWebsocketImplementation(IndexedComm comm);

	/**
	 * TODO this feature is not independent of SERVER vs client layout. Must be redesigned
	 * @param commId
	 */
	void unregisterCustomWebsocketImplementation(String commId);
}
