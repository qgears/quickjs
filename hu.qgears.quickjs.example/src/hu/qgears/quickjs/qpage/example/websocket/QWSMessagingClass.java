package hu.qgears.quickjs.qpage.example.websocket;

import java.nio.ByteBuffer;
import java.util.TimerTask;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import hu.qgears.commons.SafeTimerTask;
import hu.qgears.commons.UtilTimer;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.quickjs.qpage.IndexedComm;
import hu.qgears.quickjs.qpage.Msg;

public class QWSMessagingClass extends WebSocketAdapter implements IndexedComm.ConnectedSocket {
	public static long pingPeriodMillis = IndexedComm.timeoutPingMillis;
	private long pingCtr = 0;
	private IndexedComm indexedComm;

	public void setIndexedComm(IndexedComm indexedComm) {
		this.indexedComm = indexedComm;
	}

	@Override
	public void onWebSocketConnect(Session sess) {
		// super function stores the session object. If not called then getSession will
		// return null.
		super.onWebSocketConnect(sess);
		if (indexedComm == null) {
			sess.getRemote().sendStringByFuture("{\"error\":\"server side message queue does not exist\"}");
			UtilTimer.javaTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					sess.close();
				}
			}, 2000);
		} else {
			indexedComm.socketConnected(this);
			UtilTimer.javaTimer.scheduleAtFixedRate(new SafeTimerTask() {
				@Override
				public void doRun() {
					try {
						String data = "" + pingCtr++;
						ByteBuffer payload = ByteBuffer.wrap(data.getBytes());
						if (sess.isOpen()) {
							sess.getRemote().sendPing(payload);
						} else {
							this.cancel();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, pingPeriodMillis, pingPeriodMillis);
		}
	}

	@Override
	public void onWebSocketText(String message) {
		if(indexedComm!=null)
		{
			indexedComm.socketReceived(message);
		}
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		// We do not handle close event: resource is freed by timer
		close();
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		// We do not handle error event: resource is freed by timer
		System.out.println("WS error "+cause);
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		if(indexedComm!=null)
		{
			indexedComm.socketReceived(payload, offset, len);
		}
	}

	@Override
	public void send(Msg msg) {
		RemoteEndpoint re=getSession().getRemote();
		// Sends must be synchronized otherwise pong and ack messages could mix up with
		// Multi-part messages and cause trouble.
		synchronized (this) {
			re.sendStringByFuture(msg.header.toString());
			if(msg.arguments!=null)
			{
				for(Object o:msg.arguments)
				{
					if(o instanceof String)
					{
						re.sendStringByFuture((String)o);
					}else if(o instanceof byte[])
					{
						re.sendBytesByFuture(ByteBuffer.wrap((byte[])o));
					}else if(o instanceof INativeMemory)
					{
						@SuppressWarnings("resource")
						INativeMemory nm=(INativeMemory) o;
						re.sendBytesByFuture(nm.getJavaAccessor().duplicate());
					}else
					{
						throw new RuntimeException("Uknown type to send: "+(o==null?"null":o.getClass().toString())+" "+o);
					}
				}
			}
		}
	}

	@Override
	public void close() {
		getSession().close();
	}
}
