package hu.qgears.quickjs.spa;

import hu.qgears.quickjs.qpage.IQPageFactory;

public class RoutingEndpointQPage extends RoutingEndpoint {
	public final IQPageFactory fact;
	public RoutingEndpointQPage(IQPageFactory fact) {
		super();
		this.fact = fact;
	}
}
