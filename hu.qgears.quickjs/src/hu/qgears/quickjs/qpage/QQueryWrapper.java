package hu.qgears.quickjs.qpage;

import hu.qgears.quickjs.spa.RoutingEndpoint;

/** Interface of the web http query that creates this QPage. Abstraction over the Request servlet API
 *  so that it is possible to use different http server APIs. */
public interface QQueryWrapper {

	String getMethod();
	
	boolean executeEndpoint(RoutingEndpoint re, String path, int pathAt);

}
