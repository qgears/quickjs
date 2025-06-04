package hu.qgears.quickjs.tcpwebsocketproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilFile;

public class WebSocketProxyConnectedSocket {
	WebSocketProxy webSocketProxy;
	Supplier<Socket> sc;
	volatile Socket s;
	String id;
	private volatile boolean closed;
	private OutputStream os;
	public final UtilEvent<WebSocketProxyConnectedSocket> eventClosed=new UtilEvent<>();
	public WebSocketProxyConnectedSocket(WebSocketProxy webSocketProxy, Supplier<Socket> sc, String id) throws IOException {
		this.webSocketProxy=webSocketProxy;
		this.sc=sc;
		this.id=id;
		s=sc.get();
		os=s.getOutputStream();
		new Thread() {
			public void run() {
				byte[] data=new byte[UtilFile.defaultBufferSize.get()];
				try {
					InputStream is=s.getInputStream();
					webSocketProxy.l.connected(WebSocketProxyConnectedSocket.this);
					if(closed)
					{
						s.close();
					}
					int n=1;
					while(n>0)
					{
						n=is.read(data);
						if(n>0)
						{
							synchronized (webSocketProxy.sendSync) {
								webSocketProxy.l.data(data, 0, n, id, false);
								webSocketProxy.getSession().getRemote().sendString("D"+id);
								webSocketProxy.getSession().getRemote().sendBytes(ByteBuffer.wrap(data, 0, n));
							}
						}
					}
				} catch(SocketException sex)
				{
					// Socket closed is not logged
					webSocketProxy.log.info("Socket closed: "+sex.getMessage());
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
		}.start();
	}
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		try {
			os.write(payload, offset, len);
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			close(true);
		}
	}
	public void close(boolean sendClose) {
		if(!closed)
		{
			closed=true;
			if(s!=null)
			{
				try {
					s.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			webSocketProxy.connectionClosed(id, sendClose);
			eventClosed.eventHappened(this);
		}
	}
	public void messageError(String msg) {
		System.err.println("Error: "+msg);
	}
}
