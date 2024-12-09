package example;
public class RemotingServer extends hu.qgears.quickjs.serialization.RemotingServer {
	private example.All publishedObject;
	public void setRpublishedObject(example.All publishedObject) {
		this.publishedObject = publishedObject;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Object callFromClient(hu.qgears.quickjs.serialization.IQCallContext context, String iface, String methodPrototype, Object[] arg) throws Exception
	{
		switch(iface)
		{
		case "getRemote":
		switch(methodPrototype)
		{
			case "findAll(java.lang.String)":
			{
				return publishedObject.getRemote().findAll((java.lang.String) arg[0]);
			}
			case "testit()":
			{
				return publishedObject.getRemote().testit();
			}
			case "mycallback(hu.qgears.quickjs.serialization.IRemotingCallback)":
			{
				publishedObject.getRemote().mycallback(context, (hu.qgears.quickjs.serialization.IRemotingCallback<java.lang.String>) arg[0]);
				return null;
			}
			case "masik(java.util.Set)":
			{
				publishedObject.getRemote().masik((java.util.Set<java.lang.Integer>) arg[0]);
				return null;
			}
			case "harmadik(java.util.Set,java.lang.String)":
			{
				return publishedObject.getRemote().harmadik((java.util.Set<java.lang.Integer>) arg[0], (java.lang.String) arg[1]);
			}
			case "alma(int)":
			{
				return publishedObject.getRemote().alma((int) arg[0]);
			}
			case "alma(java.lang.String)":
			{
				return publishedObject.getRemote().alma((java.lang.String) arg[0]);
			}
			default:
				return super.callFromClient(context, iface, methodPrototype, arg);
		}
			default:
				return super.callFromClient(context, iface, methodPrototype, arg);				
		}
	}
}
