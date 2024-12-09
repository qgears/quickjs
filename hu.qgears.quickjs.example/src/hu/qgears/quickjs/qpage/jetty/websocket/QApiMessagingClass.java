package hu.qgears.quickjs.qpage.jetty.websocket;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.qpage.DisposableContainer;
import hu.qgears.quickjs.qpage.jetty.QPageHandler;
import hu.qgears.quickjs.serialization.CallbackOnRemote;
import hu.qgears.quickjs.serialization.IRemotingCallbackHost;
import hu.qgears.quickjs.serialization.RemoteMessageObject;
import hu.qgears.quickjs.serialization.RemotingServer;
import hu.qgears.quickjs.serialization.SerializeBase;
import hu.qgears.quickjs.serverside.QCallContext;
import hu.qgears.quickjs.utils.gdpr.GdprSession;

public class QApiMessagingClass extends WebSocketAdapter implements IRemotingCallbackHost {
	QPageHandler handler;
	SerializeBase serialize;
	private RemotingServer remotingServer;
	@SuppressWarnings("rawtypes")
	private Map<Integer, CallbackOnRemote> callbacks=new HashMap<>();
	DisposableContainer disposableContainer=new DisposableContainer();
	Logger log=LoggerFactory.getLogger(getClass());
	public QCallContext context;
	public QApiMessagingClass(QPageHandler handler, QCallContext context) {
		this.handler=handler;
		this.context=context;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		ByteBuffer bb=ByteBuffer.wrap(payload);
		bb.position(offset);
		bb.limit(offset+len);
		serialize.setInput(bb);
		RemoteMessageObject o=(RemoteMessageObject)serialize.deserializeObject();
		// System.out.print("Received message: "+o);
		switch(o.type)
		{
		case RemoteMessageObject.TYPE_CALL:
			try {
				CallbackOnRemote cb=null;
				for(Object ob: o.argumentsToSerialize)
				{
					if(ob instanceof CallbackOnRemote)
					{
						cb=(CallbackOnRemote) ob;
						cb.setHost(this);
					}
				}
				Object ret=remotingServer.callFromClient(context, o.iface, o.methodPrototype, o.argumentsToSerialize);
				if(cb!=null)
				{
					cb.setCloseable((NoExceptionAutoClosable) ret);
					registerCallback(cb);
					ret=null;
				}
				RemoteMessageObject reply=new RemoteMessageObject(RemoteMessageObject.TYPE_RETURN, o.callbackId, ret);
				serialize.reset();
				serialize.serializeObject(reply);
				byte[] data=serialize.getOutput().getDataCopy();
				getSession().getRemote().sendBytes(ByteBuffer.wrap(data));
			} catch (Exception e) {
				log.error("Remote call: "+o.iface+"."+o.methodPrototype, e);
				// TODO send error to client
/*				RemoteMessageObject reply=new RemoteMessageObject(RemoteMessageObject.TYPE_RETURN, o.callbackId, ret);
				serialize.reset();
				serialize.serializeObject(reply);
				*/
			}
			break;
		case RemoteMessageObject.TYPE_DISPOSE_CALLBACK:
			break;
		default:
			throw new RuntimeException("Unhandled message type: "+o.type);
		}
	}
	@SuppressWarnings("rawtypes")
	private void registerCallback(CallbackOnRemote cb) {
		CallbackOnRemote prev;
		synchronized (this) {
			if(callbacks==null)
			{
				prev=cb;
			}else
			{
				prev=callbacks.put(cb.getIndex(), cb);
			}
		}
		if(prev!=null)
		{
			prev.dispose();
		}
	}
	@SuppressWarnings("rawtypes")
	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		disposableContainer.close();
		Map<Integer, CallbackOnRemote> callbacks;
		synchronized (this) {
			callbacks=this.callbacks;
			this.callbacks=null;
		}
		for(CallbackOnRemote v: callbacks.values())
		{
			v.dispose();
		}
	}
	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		serialize=handler.getContextConfigurator().createSerializator();
		remotingServer=handler.getContextConfigurator().createRemotingServer(disposableContainer, context.getSession());
	}
	@Override
	public void onWebSocketError(Throwable cause) {
		// getSession().close(new CloseStatus(closeCode, reasonPhrase));
		// TODO Auto-generated method stub
		super.onWebSocketError(cause);
	}
	@Override
	public void onWebSocketText(String message) {
		System.out.println("On Websocket Text "+message);
	}
	public static AbstractHandler createHandler(QPageHandler handler) {
		WebSocketSimple wss=new WebSocketSimple(new WebSocketCreator() {
			@Override
			public Object createWebSocket(ServletUpgradeRequest arg0, ServletUpgradeResponse arg1) {
				HttpServletRequest r=arg0.getHttpServletRequest();
				HttpSession  sess= r.getSession();
				QCallContext context=new QCallContext();
				context.setSession((GdprSession) sess);
				return new QApiMessagingClass(handler, context);
			}
		});
		wss.setIdleTimeout(QWSMessagingClass.pingPeriodMillis*2);
		wss.setMaxSize(65536);
		return wss.createHandler();
	}
	@Override
	public <T> void callback(int index, T value) {
		try {
			// Create new instance because the serializator is not thread safe and callbacks can come from any thread
			SerializeBase serialize=handler.getContextConfigurator().createSerializator();
			RemoteMessageObject rmo=new RemoteMessageObject(RemoteMessageObject.TYPE_CALL_CALLBACK, index, value);
			serialize.reset();
			serialize.serializeObject(rmo);
			byte[] data=serialize.getOutput().getDataCopy();
			RemoteEndpoint rep=getRemote();
			rep.sendBytes(ByteBuffer.wrap(data));
		} catch (Exception e) {
			log.error("Callback "+index, e);
		}
	}
}
