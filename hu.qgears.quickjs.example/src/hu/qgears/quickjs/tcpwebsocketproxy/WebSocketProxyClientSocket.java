package hu.qgears.quickjs.tcpwebsocketproxy;

import java.io.IOException;
import java.net.Socket;

import hu.qgears.commons.UtilEvent;

public class WebSocketProxyClientSocket {
	WebSocketProxy webSocketProxy;
	String host;
	int port;
	String id;
	private volatile boolean closed=false;
	public final UtilEvent<WebSocketProxyClientSocket> eventClosed=new UtilEvent<>();
	public WebSocketProxyClientSocket(WebSocketProxy webSocketProxy, String id, String host, int port) {
		this.webSocketProxy=webSocketProxy;
		this.host=host;
		this.id=id;
		this.port=port;
	}

	public Socket createSocket()
	{
		try {
			Socket s=new Socket(host, port);
			return s;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public void close(boolean sendClose)
	{
		if(!closed)
		{
			closed=true;
			webSocketProxy.clientClosed(id, sendClose);
			eventClosed.eventHappened(this);
		}
	}
}
