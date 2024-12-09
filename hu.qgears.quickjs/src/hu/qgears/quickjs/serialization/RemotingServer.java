package hu.qgears.quickjs.serialization;

public class RemotingServer {
	public Object callFromClient(IQCallContext context, String iface, String methodPrototype, Object[] arg) throws Exception
	{
		throw new RuntimeException("Not existing: "+arg[0]);
	}
}
