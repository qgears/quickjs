package hu.qgears.quickjs.serialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.helpers.PromiseImpl;
import hu.qgears.quickjs.qpage.EQPageMode;
import hu.qgears.quickjs.qpage.IQPageContaierContext;
import hu.qgears.quickjs.qpage.QPage;

/** Remoting implementaiton for when the "client" is running in the same JVM as the server.
 *  Used for initial page generation of hybrid mode or for whole life cycle of server side mode. */
public class RemotingImplementationLocal implements IRemotingImplementation {
	private RemotingServer remotingServer;
	private Map<Integer, CallbackRegistryEntry> callbackRegistry=new HashMap<Integer, CallbackRegistryEntry>();
	private int nextCallbackRegistryIndex;
	private QPage page;
	private EQPageMode mode=EQPageMode.serverside;
	private SerializeBase serialize;
	private int callCounter=0;
	private IQCallContext context;
	private Logger log=LoggerFactory.getLogger(getClass());
	public ExecutorService executorService;
	public RemotingImplementationLocal(IQCallContext context)
	{
		this.context=context;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> void executeRemoteCall(Class<T> returnType, PromiseImpl<T> remotingReturnValue, String iface, String methodPrototype,
			Object[] argumentsToSerialize, boolean isRegisterCallback) {
		int id=callCounter++;
		if(mode==EQPageMode.hybrid)
		{
			addReplayObject(new RemoteMessageObject(id, iface, methodPrototype, argumentsToSerialize));
		}
		@SuppressWarnings("resource")
		CallbackRegistryEntry cre=null; 
		for(int i=0;i<argumentsToSerialize.length;++i)
		{
			Object o=argumentsToSerialize[i];
			if(o instanceof CallbackRegistryEntry)
			{
				cre=(CallbackRegistryEntry)o; 
				CallbackRegistryEntry creFinal=cre;
				argumentsToSerialize[i]=new IRemotingCallback() {
					@Override
					public void callback(Object value) {
						if(mode==EQPageMode.hybrid)
						{
							addReplayObject(new RemoteMessageObject(RemoteMessageObject.TYPE_CALL_CALLBACK, creFinal.id, value));
						}
						if(page.getPageContainer().getPlatform().isQPageThread())
						{
							((IRemotingCallback)creFinal.callback).callback(value);
						}else
						{
							page.getPageContainer().submitToUI(()->{((IRemotingCallback)creFinal.callback).callback(value);});
						}
					}
				};
				cre.closed.thenApply(e->{
					removeCallbackEntry(creFinal);
				});
			}
		}
		CallbackRegistryEntry creFinal=cre;
		Runnable executeCall=new Runnable() {
			@Override
			public void run() {
				Object ret=null;
				Exception ex=null;
				try {
					ret=remotingServer.callFromClient(context, iface, methodPrototype, argumentsToSerialize);
				} catch (Exception e) {
					log.error("Error local remoting", e);
					ex=e;
				}
				if(page.getPageContainer().getPlatform().isQPageThread())
				{
					signalReply(id, ret, ex, isRegisterCallback, creFinal, remotingReturnValue);
				}else
				{
					Object retFinal=ret;
					Exception exFinal=ex;
					page.getPageContainer().submitToUI(()->{signalReply(id, retFinal, exFinal, isRegisterCallback, creFinal, remotingReturnValue);});
				}
			}
		};
		if(executorService==null)
		{
			executeCall.run();
		}else
		{
			executorService.execute(executeCall);
		}
		return;
	}
	@SuppressWarnings("unchecked")
	protected <T> void signalReply(int id, Object ret, Exception ex, boolean isRegisterCallback, CallbackRegistryEntry creFinal,
			PromiseImpl<T> remotingReturnValue) {
		if(ex==null)
		{
			if(ret==null && isRegisterCallback)
			{
				NoExceptionAutoClosable c=(NoExceptionAutoClosable) ret;
				creFinal.closed.thenApply(e->{c.close();});
			}
			if(ret!=null && mode==EQPageMode.hybrid)
			{
				Object o;
				if(isRegisterCallback)
				{
					o=null;
				}else
				{
					o=ret;
				}
				addReplayObject(new RemoteMessageObject(RemoteMessageObject.TYPE_RETURN, id, o));
			}
			remotingReturnValue.ready((T) ret);
		} else {
			remotingReturnValue.error(ex);
		}
	}
	private void removeCallbackEntry(CallbackRegistryEntry entry) {
		callbackRegistry.remove(entry.id, entry);
//		if(mode==EQPageMode.hybrid)
//		{
//			RemoteMessageObject rmo=new RemoteMessageObject(RemoteMessageObject.TYPE_DISPOSE_CALLBACK, entry.id);
//			addReplayObject(rmo);
//		}
	}
	private void addReplayObject(RemoteMessageObject replayObject) {
		List<byte[]> replayObjects=page.getPageContainer().getPlatform().getReplayObjects();
		replayObjects.add(serialize(replayObject));
//		byte[] data=replayObjects.get(replayObjects.size()-1);
//		serialize.setInput(ByteBuffer.wrap(data));
//		Object o=serialize.deserializeObject();
//		String s=o.toString();
//		System.out.println("Replay object: "+s);
	}
	private byte[] serialize(RemoteMessageObject replayObject) {
		serialize.reset();
		serialize.serializeObjectAssert(replayObject);
		return serialize.getOutput().getDataCopy();
	}
	public void setRemotingServer(RemotingServer remotingServer) {
		this.remotingServer = remotingServer;
	}
	@Override
	public CallbackRegistryEntry registerCallback(String methodPrototype, IRemotingCallback<?> arg0) {
		CallbackRegistryEntry entry=new CallbackRegistryEntry(nextCallbackRegistryIndex, methodPrototype, arg0);
		callbackRegistry.put(entry.id, entry);
		nextCallbackRegistryIndex++;
		return entry;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> void callback(int index, T value) {
		CallbackRegistryEntry e=callbackRegistry.get(index);
		((IRemotingCallback)e.callback).callback(value);
		if(mode==EQPageMode.hybrid)
		{
			addReplayObject(new RemoteMessageObject(RemoteMessageObject.TYPE_CALL_CALLBACK, index, value));
		}
	}
	public void setHost(QPage page, IQPageContaierContext context) {
		this.page=page;
	}
	public void setMode(EQPageMode mode) {
		this.mode=mode;
	}
	public void setSerializator(SerializeBase serialize) {
		this.serialize=serialize;
	}
}
