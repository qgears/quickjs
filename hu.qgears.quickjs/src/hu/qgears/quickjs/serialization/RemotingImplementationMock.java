package hu.qgears.quickjs.serialization;

import java.nio.ByteBuffer;

import hu.qgears.quickjs.helpers.PromiseImpl;

public class RemotingImplementationMock implements IRemotingImplementation {
	private SerializeBase serialize;
	private RemotingServer remotingServer;
	
	public void setSerialize(SerializeBase serialize) {
		this.serialize = serialize;
	}
	@Override
	public <T> void executeRemoteCall(Class<T> returnType, PromiseImpl<T> remotingReturnValue, String methodPrototype,
			Object[] argumentsToSerialize) {
		serialize.reset();
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
	private ByteBuffer executeBySerialized(ByteBuffer bb) {
		serialize.setInput(bb);
		String mp=(String)serialize.deserializeObject();
		Object[] args=(Object[])serialize.deserializeObject();
		Object ret=remotingServer.callFromClient(mp, args);
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
}
