package hu.qgears.quickjs.spa;

import hu.qgears.quickjs.qpage.IQPageFactory2;

public class RoutingEndpointQPage extends RoutingEndpoint {
	public final IQPageFactory2 fact;
	public RoutingEndpointQPage(IQPageFactory2 fact) {
		super();
		this.fact = fact;
	}
}
