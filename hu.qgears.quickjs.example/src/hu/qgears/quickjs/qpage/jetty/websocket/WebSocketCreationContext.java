package hu.qgears.quickjs.qpage.jetty.websocket;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;

public class WebSocketCreationContext {
	public Request r;
	public UpgradeRequest req;
	public UpgradeResponse resp;
}
