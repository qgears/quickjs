package hu.qgears.quickjs.qpage;

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONObject;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilListenableProperty;

/**
 * Indexed communication implementation with reconnect feature without message loss.
 * See also: indexedComm.js
 */
public class IndexedComm {
	public interface ConnectedSocket
	{
		void send(Msg msg);

		void close();
	}
	public static final long timeoutPingMillis=20000;
	private ConnectedSocket socket;
	private long currentIndex=0;
	private long currentReceivingIndex=0;
	private volatile Msg currentReceiving;
	private SortedMap<Long, Msg> sendQueue=Collections.synchronizedSortedMap(new TreeMap<>());
	public final UtilEvent<Msg> received=new UtilEvent<>();
	public final UtilEvent<JSONObject> receivedPing=new UtilEvent<>();
	public final UtilListenableProperty<Boolean> closed=new UtilListenableProperty<Boolean>(false);
	/**
	 * 
	 * @param header
	 * @param arguments byte[] or String
	 */
	public void sendMessage(String header, Object... arguments)
	{
		if(closed.getProperty())
		{
			return;
		}
		synchronized (sendQueue) {
			Msg msg=new Msg();
			msg.index=currentIndex++;
			JSONObject obj=new JSONObject();
			obj.put("nPart", arguments.length);
			obj.put("index", msg.index);
			obj.put("header", header);
			msg.header=obj;
			msg.arguments=arguments;
			sendQueue.put(msg.index, msg);
			if(socket!=null)
			{
				socket.send(msg);
			}
			if(logMessages)
			{
				msg.log();
			}
		}
	}
	public void socketConnected(ConnectedSocket socket)
	{
		synchronized (sendQueue) {
			this.socket=socket;
			for(Msg msg: sendQueue.values())
			{
				socket.send(msg);
			}
		}
	}
	public void socketReceived(String s)
	{
		try {
			if(currentReceiving==null)
			{
				JSONObject obj=new JSONObject(s);
				if(obj.has("ack"))
				{
					handleAck(obj);
					return;
				}else if(obj.has("ping"))
				{
					handlePing(obj);
					return;
				}else
				{
					JSONObject header=obj.getJSONObject("header");
					int nPart=obj.getInt("nPart");
					int index=obj.getInt("index");
					currentReceiving=new Msg();
					currentReceiving.index=index;
					currentReceiving.nPart=nPart;
					currentReceiving.header=header;
					if(nPart>0)
					{
						currentReceiving.arguments=new Object[nPart];
					}
				}
			}else
			{
				currentReceiving.arguments[currentReceiving.currentArgument]=s;
				currentReceiving.currentArgument++;
			}
			checkFinished();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void handlePing(JSONObject obj) {
		Msg ackMsg=new Msg();
		ackMsg.header=new JSONObject();
		((JSONObject)ackMsg.header).put("pong", obj.get("ping"));
		socket.send(ackMsg);
		receivedPing.eventHappened(obj);
	}
	private void checkFinished() {
		if(currentReceiving.nPart<=currentReceiving.currentArgument)
		{
			try {
				sendAck(currentReceiving);
				if(currentReceivingIndex<=currentReceiving.index)
				{
					currentReceivingIndex=currentReceiving.index;
					received.eventHappened(currentReceiving);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			currentReceiving=null;
		}
	}
	private void sendAck(Msg currentReceiving) {
		Msg ackMsg=new Msg();
		ackMsg.header=new JSONObject();
		((JSONObject)ackMsg.header).put("ack", currentReceiving.index);
		socket.send(ackMsg);
	}
	private void handleAck(JSONObject obj) {
		long index=obj.getLong("ack");
		Long fk;
		if(sendQueue.isEmpty())
		{
			fk=null;
		}else
		{
			fk=sendQueue.firstKey();
		}
		while(fk!=null && fk<=index)
		{
			Msg toDispose=sendQueue.remove(fk);
			if(toDispose!=null)
			{
				for(Object arg: toDispose.arguments)
				{
					if(arg instanceof AutoCloseable)
					{
						AutoCloseable nm=(AutoCloseable) arg;
						try {
							nm.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			if(sendQueue.isEmpty())
			{
				fk=null;
			}else
			{
				fk=sendQueue.firstKey();
			}
		}
	}
	public void socketReceived(byte[] data, int offset, int len)
	{
		currentReceiving.arguments[currentReceiving.currentArgument]=Arrays.copyOfRange(data, offset, offset+len);
		currentReceiving.currentArgument++;
		checkFinished();
	}
	public void close() {
		closed.setProperty(true);
		if(socket!=null)
		{
			socket.close();
		}
	}
	private volatile boolean logMessages=false;
	public void setLogMessages(boolean b) {
		this.logMessages=b;
	}
}
