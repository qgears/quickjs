package example;
public class RemotingServer extends hu.qgears.quickjs.serialization.RemotingServer {
	private example.ExampleRemoteIf publishedObject;
	public void setRpublishedObject(example.ExampleRemoteIf publishedObject) {
		this.publishedObject = publishedObject;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Object callFromClient(String methodPrototype, Object[] arg)
	{
		switch(methodPrototype)
		{
			case "findAll(java.lang.String)":
			{
				return publishedObject.findAll((java.lang.String) arg[0]);
			}
			case "masik(java.util.Set)":
			{
				publishedObject.masik((java.util.Set<java.lang.Integer>) arg[0]);
				return null;
			}
			case "harmadik(java.util.Set,java.lang.String)":
			{
				return publishedObject.harmadik((java.util.Set<java.lang.Integer>) arg[0], (java.lang.String) arg[1]);
			}
			case "alma(int)":
			{
				return publishedObject.alma((int) arg[0]);
			}
			case "testit()":
			{
				return publishedObject.testit();
			}
			case "alma(java.lang.String)":
			{
				return publishedObject.alma((java.lang.String) arg[0]);
			}
			default:
				return super.callFromClient(methodPrototype, arg);
		}
	}
}
