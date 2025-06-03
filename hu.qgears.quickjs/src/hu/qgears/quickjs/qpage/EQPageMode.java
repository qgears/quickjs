package hu.qgears.quickjs.qpage;

/** Mode to run the QPage
 */
public enum EQPageMode {
	/** Run the QPage logic (AbstractQPage) on the server side: useful for debugging and simpler to configure than client side.
	 *  This is the default if not set. */
	serverside,
	/** Run the QPage logic (AbstractQPage) on both sides "hybrid" mode: 
	 *  * useful for SEO (compared to client side mode) because initial reply can contain valid content.
	 *  * useful for server resources because after initial generation the server is not involved with client logic (only the remoting APIs call into the server) */
	hybrid,
	/** TODO client only mode is not supported yet because it is not really useful. */
	//clientside,
}
