package hu.qgears.quickjs.tcpwebsocketproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import hu.qgears.commons.UtilEvent;

public class WebSocketProxyServerSocket {
	WebSocketProxy webSocketProxy;
	String serverSocketId;
	private String conf;
	private int port;
	volatile boolean closed;
	volatile ServerSocket ss;
	public final UtilEvent<WebSocketProxyServerSocket> eventClosed=new UtilEvent<>();
	public WebSocketProxyServerSocket(WebSocketProxy webSocketProxy, String id, String conf) {
		this.webSocketProxy=webSocketProxy;
		this.serverSocketId=id;
		this.conf=conf;
	}

	public void close(boolean sendClose)
	{
		if(!closed)
		{
			closed=true;
			webSocketProxy.serverClosed(serverSocketId, sendClose);
			eventClosed.eventHappened(this);
			if(ss!=null)
			{
				try {
					ss.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void start() {
		new Thread()
		{
			public void run() {
				try {
					try(ServerSocket ss=new ServerSocket())
					{
						WebSocketProxyServerSocket.this.ss=ss;
						ss.bind(new InetSocketAddress("localhost", 0));
						port=ss.getLocalPort();
						webSocketProxy.l.serverPortOpened(WebSocketProxyServerSocket.this);
						while(!closed)
						{
							Socket s=ss.accept();
							String id=webSocketProxy.createId("conn");
							WebSocketProxyConnectedSocket connected=new WebSocketProxyConnectedSocket(webSocketProxy, ()->s, id);
							webSocketProxy.createServerConnection(serverSocketId, id, connected);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally
				{
					try {
						close(true);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		}
		.start();
	}
	public int getPort() {
		return port;
	}
	public String getConf() {
		return conf;
	}
	@Override
	public String toString() {
		return getClass().getSimpleName()+" "+conf;
	}

	public void sendError(String string) {
		webSocketProxy.messageError(serverSocketId, string);
	}
}
