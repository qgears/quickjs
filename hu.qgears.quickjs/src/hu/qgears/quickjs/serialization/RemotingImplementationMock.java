package hu.qgears.quickjs.serialization;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import hu.qgears.quickjs.helpers.PromiseImpl;

/** Remoting implementation that can be used to test serialization and remoting in a single JVM. Used by auto tests.
 */
public class RemotingImplementationMock implements IRemotingImplementation {
	private Supplier<SerializeBase> serializeFact;
	private RemotingServer remotingServer;
	private Map<Integer, CallbackRegistryEntry> callbackRegistry=new HashMap<Integer, CallbackRegistryEntry>();
	private int nextCallbackRegistryIndex;
	public void setSerialize(Supplier<SerializeBase> serialize) {
		this.serializeFact = serialize;
	}
	@Override
	public <T> void executeRemoteCall(Class<T> returnType, PromiseImpl<T> remotingReturnValue, String iface, String methodPrototype,
			Object[] argumentsToSerialize, boolean isRegisterCallback) {
		SerializeBase serialize=serializeFact.get();
		serialize.reset();
		if(!serialize.serializeObject(iface))
		{
			throw new RuntimeException();
		}
		if(!serialize.serializeObject(methodPrototype))
		{
			throw new RuntimeException();
		}
		if(!serialize.serializeObject(argumentsToSerialize))
		{
			throw new RuntimeException("Not serialized type: "+argumentsToSerialize.getClass());
		}
		ByteBuffer bb=serialize.getSerializedBinary();
		new Thread("RemotingImplementationMock")
		{
			public void run() {
				try {
					Thread.sleep(5);
					ByteBuffer ret=executeBySerialized(bb);
					serialize.setInput(ret);
					Object retval=serialize.deserializeObject();
					remotingReturnValue.ready(returnType.cast(retval));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}
		.start();
	}
	@SuppressWarnings({ "rawtypes", "unchecked", "resource" })
	private void executeCallbackBySerialized(ByteBuffer bb)
	{
		SerializeBase serialize=serializeFact.get();
		serialize.setInput(bb);
		String mp=(String)serialize.deserializeObject();
		assert("callback".equals(mp));
		int index=(int)serialize.deserializeObject();
		Object arg=(Object)serialize.deserializeObject();
		((IRemotingCallback)callbackRegistry.get(index).callback).callback(arg);
	}
	private ByteBuffer executeBySerialized(ByteBuffer bb) {
		SerializeBase serialize=serializeFact.get();
		serialize.setInput(bb);
		String iface=(String)serialize.deserializeObject();
		String mp=(String)serialize.deserializeObject();
		Object[] args=(Object[])serialize.deserializeObject();
		IQCallContext context=new QCallContextMock();
		for(Object o: args)
		{
			if(o instanceof CallbackOnRemote<?>)
			{
				((CallbackOnRemote<?>) o).setHost(this);
			}
		}
		Object ret;
		try {
			ret = remotingServer.callFromClient(context, iface, mp, args);
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
			ret=null;
		}
		serialize.reset();
		if(!serialize.serializeObject(ret))
		{
			throw new RuntimeException("Can not serialize type: "+ret);
		}
		return serialize.getSerializedBinary();
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

		SerializeBase serialize=serializeFact.get();
		serialize.reset();
		if(!serialize.serializeObject("callback"))
		{
			throw new RuntimeException();
		}
		if(!serialize.serializeObject(index))
		{
			throw new RuntimeException();
		}
		if(!serialize.serializeObject(value))
		{
			throw new RuntimeException("Not serialized type: "+value.getClass());
		}
		ByteBuffer bb=serialize.getSerializedBinary();
		new Thread("RemotingImplementationMockCallback")
		{
			public void run() {
				try {
					Thread.sleep(5);
					executeCallbackBySerialized(bb);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}
		.start();

	}
}
