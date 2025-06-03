package hu.qgears.quickjs.teavm;

import hu.qgears.quickjs.qpage.QQueryWrapper;
import hu.qgears.quickjs.spa.RoutingEndpoint;
import hu.qgears.quickjs.spa.RoutingEndpointQPage;

public class TeaVMQuery implements QQueryWrapper {
	public RoutingEndpointQPage found;
	@Override
	public String getMethod() {
		return "GET";
	}
	@Override
	public boolean executeEndpoint(RoutingEndpoint re, String path, int pathAt) {
		if(re instanceof RoutingEndpointQPage)
		{
			found=(RoutingEndpointQPage)re;
			return true;
		}
		return false;
	}
}
