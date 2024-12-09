package hu.qgears.quickjs.qpage.jetty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import hu.qgears.quickjs.qpage.QQueryWrapper;
import hu.qgears.quickjs.spa.RoutingEndpoint;
import hu.qgears.quickjs.spa.RoutingEndpointQPage;

public class QueryWrapperJetty implements QQueryWrapper {
	public String target;
	public Request baseRequest;
	public HttpServletRequest request;
	public HttpServletResponse response;
	public RoutingEndpoint routingEndpoint;
	public QueryWrapperJetty(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) {
		this.target=target;
		this.baseRequest=baseRequest;
		this.request=request;
		this.response=response;
	}
	@Override
	public String getMethod() {
		return baseRequest.getMethod();
	}
	@Override
	public boolean executeEndpoint(RoutingEndpoint re, String path, int pathAt) {
		routingEndpoint=(RoutingEndpointQPage) re;
		return true;
	}
}
