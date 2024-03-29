package hu.qgears.quickjs.qpage.jetty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import hu.qgears.quickjs.qpage.QQueryWrapper;

public class QueryWrapperJetty implements QQueryWrapper {
	public String target;
	public Request baseRequest;
	public HttpServletRequest request;
	public HttpServletResponse response;
	public QueryWrapperJetty(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) {
		this.target=target;
		this.baseRequest=baseRequest;
		this.request=request;
		this.response=response;
	}

}
