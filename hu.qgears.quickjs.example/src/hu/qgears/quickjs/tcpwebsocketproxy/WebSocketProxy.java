package hu.qgears.quickjs.tcpwebsocketproxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.SafeTimerTask;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilTimer;

/**
 * Proxy a TCP port by connecting through a WebSocket.
 * 
 * Web Socket Adapter that works on both ends of the WebSocket connection.
 */
public class WebSocketProxy extends WebSocketAdapter {
	public static final int pingPeriodMillis=15000;
	Selector selector;
	Object sendSync=new Object();
	WebSocketProxyConnectedSocket currentReceive;
	private Map<String, WebSocketProxyConnectedSocket> connections=new HashMap<>();
	private Map<String, WebSocketProxyClientSocket> clientSockets=new HashMap<>();
	private Map<String, WebSocketProxyServerSocket> serverSockets=new HashMap<>();
	private AtomicInteger ids=new AtomicInteger();
	private String thisId;
	protected WebSocketProxyListener l;
	protected Logger log=LoggerFactory.getLogger(getClass());
	public final UtilEvent<Throwable> errorEvent=new UtilEvent<>();
	
	public WebSocketProxy(String thisId, WebSocketProxyListener l) {
		super();
		this.thisId = thisId;
		this.l=l;
	}
	void startServerSocket(String serverSocketId, String data)
	{
		WebSocketProxyServerSocket wpss=new WebSocketProxyServerSocket(this, serverSocketId, data);
		synchronized (wpss) {
			serverSockets.put(serverSocketId, wpss);
		}
		wpss.start();
	}
	/**
	 * 
	 * @param id
	 * @param data sent to the other side: possible conficuration
	 * @param host connect this host on incoming proxy connection
	 * @param port connect this port on incoming proxy connection
	 */
	public WebSocketProxyClientSocket createConnector(String data, String host, int port)
	{
		String id=createId("connector");
		WebSocketProxyClientSocket wpcs=new WebSocketProxyClientSocket(this, id, host, port);
		synchronized (sendSync) {
			clientSockets.put(id, wpcs);
			getSession().getRemote().sendString("P"+id+":"+data, null);
		}
		return wpcs;
	}
	String createId(String string) {
		return thisId+"-"+string+"-"+ids.incrementAndGet();
	}
	@Override
	public void onWebSocketError(Throwable cause) {
		super.onWebSocketError(cause);
		errorEvent.eventHappened(cause);
	}
	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		System.out.println("Websocket close...");
		//todo close all
		super.onWebSocketClose(statusCode, reason);
		List<WebSocketProxyServerSocket> toClose;
		synchronized (sendSync) {
			toClose=new ArrayList<>(serverSockets.values());
		}
		for(WebSocketProxyServerSocket ssocket: toClose)
		{
			ssocket.close(false);
		}
		l.disconnected(this);
	}
	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		try {
			l.clientConnected(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(pingInterval!=null)
		{
			UtilTimer.javaTimer.schedule(new SafeTimerTask() {
				@Override
				public void doRun() {
					new Thread() {
						@Override
						public void run() {
							// TODO do not block generic timer!!!!
							try {
								getRemote().sendPing(ByteBuffer.wrap(new byte[] {}));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								cancel();
							}
						}
					}.start();
				}
			}, pingInterval, pingInterval);
		}
	}
	@Override
	public void onWebSocketText(String message) {
		char command=message.charAt(0);
		switch(command)
		{
		case 'P':
		{
			int idx=message.indexOf(':');
			String id=message.substring(1, idx);
			String data=message.substring(idx+1);
			startServerSocket(id, data);
			break;
		}
		case 'C': // Connect
		{
			int idx=message.indexOf(':');
			String proxyId=message.substring(1, idx);
			String connId=message.substring(idx+1);
			log.info("Connect "+proxyId+" "+connId);
			connectionCreated(proxyId, connId);
			break;
		}
		case 'c': // Close
		{
			String connId=message.substring(1);
			log.info("Close: "+connId);
			connectionClosed(connId, false);
			serverClosed(connId, false);
			clientClosed(connId, false);
			break;
		}
		case 'e': // Error
		{
			int idx=message.indexOf(' ');
			String connId=message.substring(1, idx);
			String msg=message.substring(idx+1);
			synchronized (sendSync) {
				WebSocketProxyConnectedSocket ret=connections.get(connId);
				if(ret!=null)
				{
					log.info("Connection error "+ret.id);
					ret.messageError(msg);
				}
			}
			break;
		}
		case 'D': // Data block
			synchronized (sendSync) {
				String connId=message.substring(1);
				currentReceive=connections.get(connId);
			}
		}
		super.onWebSocketText(message);
	}
	private void connectionCreated(String proxyId, String connId) {
		WebSocketProxyClientSocket p;
		synchronized (sendSync) {
			p=clientSockets.get(proxyId);
		}
		if(p!=null)
		{
			WebSocketProxyConnectedSocket wscs;
			try {
				wscs = new WebSocketProxyConnectedSocket(this, p::createSocket, connId);
				synchronized (sendSync) {
					connections.put(connId, wscs);
				}
			} catch (IOException e) {
				//TODO log
				e.printStackTrace();
				connectionClosed(connId, true);
			}
		}
	}
	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		if(currentReceive!=null)
		{
			l.data(payload, offset, len, currentReceive.id, true);
			log.info("Data "+currentReceive.id+" "+len);
			currentReceive.onWebSocketBinary(payload, offset, len);
		}else
		{
			log.error("Current receive is null when binary was received: nbytes: "+len);
		}
		currentReceive=null;
	}
	public WebSocketProxyConnectedSocket connectionClosed(String id, boolean sendClose) {
		synchronized (sendSync) {
			WebSocketProxyConnectedSocket ret=connections.remove(id);
			if(sendClose)
			{
				try {
					getRemote().sendString("c"+id, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(ret!=null)
			{
				log.info("Connection closed "+ret.id);
				ret.close(sendClose);
			}
			return ret;
		}
	}
	public WebSocketProxyClientSocket clientClosed(String id, boolean sendClose) {
		synchronized (sendSync) {
			WebSocketProxyClientSocket ret=clientSockets.remove(id);
			if(sendClose)
			{
				try {
					getRemote().sendString("c"+id, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(ret!=null)
			{
				ret.close(sendClose);
			}
			return ret;
		}
	}
	public void createServerConnection(String serverSocketId, String id, WebSocketProxyConnectedSocket connected) throws IOException {
		synchronized (sendSync) {
			connections.put(id, connected);
			getRemote().sendString("C"+serverSocketId+":"+id);
		}
	}
	public WebSocketProxyServerSocket serverClosed(String id, boolean sendClose) {
		synchronized (sendSync) {
			WebSocketProxyServerSocket ret=serverSockets.remove(id);
			if(sendClose)
			{
				try {
					getRemote().sendString("c"+id, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(ret!=null)
			{
				ret.close(sendClose);
			}
			return ret;
		}
	}
	Integer pingInterval;
	public void setPingInterval(int i) {
		pingInterval=i;
	}
	public void messageError(String id, String string) {
		try {
			getRemote().sendString("e"+id+" "+string, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
