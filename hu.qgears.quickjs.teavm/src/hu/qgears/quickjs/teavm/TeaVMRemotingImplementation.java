package hu.qgears.quickjs.teavm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hu.qgears.quickjs.helpers.PromiseImpl;
import hu.qgears.quickjs.serialization.CallbackRegistryEntry;
import hu.qgears.quickjs.serialization.IRemotingCallback;
import hu.qgears.quickjs.serialization.IRemotingImplementation;
import hu.qgears.quickjs.serialization.RemoteMessageObject;
import hu.qgears.quickjs.serialization.SerializeBase;

public class TeaVMRemotingImplementation implements IRemotingImplementation {
	private Map<Integer, CallbackRegistryEntry> callbackRegistry=new HashMap<Integer, CallbackRegistryEntry>();
	private int nextCallbackRegistryIndex;
	private TeaVMQPageContainer pageContainer;
	private SerializeBase serialize;
	/** Calls currently awaited for reply from server. */
	private Map<Integer, PromiseImpl<?>> pendingCalls=new HashMap<>();
	/** Messages awaiting to be sent */
	private LinkedList<byte[]> toSend=new LinkedList<>();
	private int callCounter=0;
	private int replayProcessed=0;
	/** Replay list of server side initialization messages. */
	private List<RemoteMessageObject> replay;
	private boolean replayEnded;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> void executeRemoteCall(Class<T> returnType, PromiseImpl<T> remotingReturnValue, String iface,
			String methodPrototype, Object[] argumentsToSerialize, boolean isRegisterCallback) {
		int id=callCounter++;
		CallbackRegistryEntry cre=null;
		if(isRegisterCallback)
		{
			cre=callbackRegistry.get(nextCallbackRegistryIndex-1);
		}
		if(!replayEnded) {
			RemoteMessageObject ro=takeReplayObject();
			if(ro.type!=RemoteMessageObject.TYPE_CALL || !iface.equals(ro.iface) || !methodPrototype.equals(ro.methodPrototype))
			{
				throw new RuntimeException("Replay query not equal to actual query: "+ro+" actual: "+iface+" "+methodPrototype);
			}
			nextCallbackRegistryIndex=Math.max(ro.callbackId+1, nextCallbackRegistryIndex);
			// TODO check arguments assert them equal
			
			if(isRegisterCallback)
			{
				cre.payload=ro.getClientSideAsBinary();
				// System.out.println("registerCallback payload: "+cre.id+" payload length: "+cre.payload.length);
			}
			processAsyncMessages();
			RemoteMessageObject retv=takeReplayObject();
			if(retv.type!=RemoteMessageObject.TYPE_RETURN)
			{
				throw new RuntimeException("ReplayObject has to be a return object");
			}
			if(isRegisterCallback)
			{
				((PromiseImpl)remotingReturnValue).ready(cre);
			}else
			{
				((PromiseImpl)remotingReturnValue).ready(retv.ret);
			}
			processAsyncMessages();
			return;
		}
		RemoteMessageObject rmo=new RemoteMessageObject(id, iface, methodPrototype, argumentsToSerialize);
		pendingCalls.put(id, remotingReturnValue);
		serialize.reset();
		serialize.serializeObject(rmo);
		byte[] payload=serialize.getOutput().getDataCopy();
		toSend.add(payload);
		pageContainer.requestCommunicationCallback();
		if(isRegisterCallback)
		{
			cre.payload=payload;
			// System.out.println("isRegisterCallback: "+cre);
			((PromiseImpl)remotingReturnValue).ready(cre);
		}
	}
	private void processAsyncMessages() {
		// Execute all callbacks that were triggered by the call
		while(hasMoreReplayObject() && peekReplayObject().type==RemoteMessageObject.TYPE_CALL_CALLBACK)
		{
			RemoteMessageObject callback=takeReplayObject();
			int bcid=callback.callbackId;
			Object value=callback.ret;
			callback(bcid, value);
		}
	}
	private boolean hasMoreReplayObject() {
		return replayProcessed<replay.size();
	}
	private RemoteMessageObject takeReplayObject() {
		RemoteMessageObject ro=replay.get(replayProcessed);
		replayProcessed++;
		return ro;
	}
	private RemoteMessageObject peekReplayObject() {
		RemoteMessageObject ro=replay.get(replayProcessed);
		return ro;
	}
	public void setSerializator(SerializeBase serialize) {
		this.serialize=serialize;
	}
	@Override
	public CallbackRegistryEntry registerCallback(String methodPrototype, IRemotingCallback<?> arg0) {
		CallbackRegistryEntry entry=new CallbackRegistryEntry(nextCallbackRegistryIndex, methodPrototype, arg0);
		callbackRegistry.put(entry.id, entry);
		nextCallbackRegistryIndex++;
		entry.closed.thenApply(e->{
			removeCallbackEntry(entry);
		});
		// System.out.println("registerCallback: "+entry.id);
		return entry;
	}
	private void removeCallbackEntry(CallbackRegistryEntry entry) {
		callbackRegistry.remove(entry.id, entry);
		RemoteMessageObject rmo=new RemoteMessageObject(RemoteMessageObject.TYPE_DISPOSE_CALLBACK, entry.id);
		serialize.reset();
		serialize.serializeObject(rmo);
		byte[] payload=Arrays.copyOf(serialize.getOutput().getData(), serialize.getOutput().getLength());
		toSend.add(payload);
		pageContainer.requestCommunicationCallback();
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> void callback(int index, T value) {
		CallbackRegistryEntry e=callbackRegistry.get(index);
		if(e==null)
		{
			throw new RuntimeException("Callback registry entry does not exist: "+index);
		}
		((IRemotingCallback)e.callback).callback((Object)value);
	}
	public void setPageContainer(TeaVMQPageContainer pageContainer) {
		this.pageContainer=pageContainer;
	}
	public void setReplayObjects(List<RemoteMessageObject> replay) {
		this.replay=replay;
	}
	public void signalReplayEnded() {
		replayEnded=true;
		if(replayProcessed!=replay.size())
		{
			throw new RuntimeException("Replay not fully processed after initial creation of HTML (signals internal error of QuickJS mechanisms or non-deterministic setup phase of the page) "+replayProcessed+"!="+replay.size());
		}
		if(pendingCalls.size()>0)
		{
			throw new RuntimeException("pendingCalls after replay is not empty (signals internal error of QuickJS mechanisms or non-deterministic setup phase of the page) "+replayProcessed+"!="+replay.size());
		}
		if(callbackRegistry.size()>0)
		{
			pageContainer.requestCommunicationCallback();
		}
	}
	public void channelOpened() {
		for(CallbackRegistryEntry e: callbackRegistry.values())
		{
			serialize.setInput(ByteBuffer.wrap(e.payload));
			RemoteMessageObject m=(RemoteMessageObject)serialize.deserializeObject();
			// Even when replaying the message the pending call is mandatory!
			pendingCalls.put(m.callbackId, new PromiseImpl<>());
			pageContainer.sendRemoteCall(e.payload);
		}
		channelReady();
	}
	public void channelReady() {
		while(!toSend.isEmpty())
		{
			byte[] data=toSend.removeFirst();
			pageContainer.sendRemoteCall(data);
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void messageReceived(byte[] data) {
		serialize.setInput(ByteBuffer.wrap(data));
		RemoteMessageObject rmo=(RemoteMessageObject)serialize.deserializeObject();
		switch(rmo.type)
		{
		case RemoteMessageObject.TYPE_RETURN:
			PromiseImpl pi=pendingCalls.remove(rmo.callbackId);
			if(pi==null)
			{
				System.out.println("Message payload: ");
				System.out.println(rmo);
				throw new RuntimeException("pendingCalls did not contain call id: "+rmo.callbackId+" ");
			}
			pi.ready(rmo.ret);
			break;
		case RemoteMessageObject.TYPE_CALL_CALLBACK:
			CallbackRegistryEntry cre=callbackRegistry.get(rmo.callbackId);
			((IRemotingCallback)cre.callback).callback(rmo.ret);
			break;
		default:
			throw new RuntimeException("Unhandled type: "+rmo.type);
		}
	}
	public void channelError() {
		// TODO Auto-generated method stub
	}
	@SuppressWarnings({ "rawtypes", "unused" })
	public void channelClosed() {
		List<PromiseImpl> toError=new ArrayList<>(pendingCalls.values());
		pendingCalls.clear();
		for(PromiseImpl pi: pendingCalls.values())
		{
			pi.error(new RuntimeException("Connection channel closed"));
		}
	}
}
