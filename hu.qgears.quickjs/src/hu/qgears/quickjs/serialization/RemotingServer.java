package hu.qgears.quickjs.serialization;

public class RemotingServer {
	public Object callFromClient(String methodPrototype, Object[] arg)
	{
		throw new RuntimeException("Not existing: "+arg[0]);
	}
}
