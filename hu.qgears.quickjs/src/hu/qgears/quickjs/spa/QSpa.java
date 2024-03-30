package hu.qgears.quickjs.spa;

/** Single page application domain. Collects all pages that are accessible without reload.
 * In case of a traditional webapp (no SPA) each AbstractQPage has to be wrapped into one QSpa */
public class QSpa extends Routing {
	/**
	 * Configure additional Websocket connections on the server that are accessible
	 * from the client.
	 * TODO specify and implement this feature.
	 * @param qPageHandler2
	 */
	public void configureWebsocketHandlers(Object qPageHandler2) {
		// TODO Auto-generated method stub
		
	}
}
