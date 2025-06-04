package hu.qgears.quickjs.qpage.jetty;

import hu.qgears.quickjs.qpage.QQueryWrapper;
import hu.qgears.quickjs.spa.RoutingEndpoint;

public interface IEndPointHandler {
	boolean handle(QQueryWrapper q, RoutingEndpoint re, String path, int pathAt);
}
