package hu.qgears.quickjs.tcpwebsocketproxy;

public interface WebSocketProxyListener {

	void serverPortOpened(WebSocketProxyServerSocket webSocketProxyServerSocket);

	void connected(WebSocketProxyConnectedSocket webSocketProxyConnectedSocket);

	void clientConnected(WebSocketProxy p);

	default void data(byte[] payload, int offset, int len, String id, boolean fromWs) {}

	void disconnected(WebSocketProxy webSocketProxy);

}
