package hu.qgears.quickjs.serialization;

/** Remoting objects for sending through WebSocked and 
 *  client side replay in case of EQPageMode.hybrid mode */
public class RemoteMessageObject {
	public static final int TYPE_CALL=0;
	// public static final int TYPE_CREATE_CALLBACK=1;
	public static final int TYPE_CALL_CALLBACK=2;
	public static final int TYPE_RETURN=3;
	public static final int TYPE_DISPOSE_CALLBACK=4;
	public int type;
	public int callbackId;
	public String iface;
	public String methodPrototype;
	public Object[] argumentsToSerialize;
	public Object ret;
	private byte[] asBinary;
	public RemoteMessageObject()
	{
	}
	public RemoteMessageObject(int callId, String iface, String methodPrototype, Object[] argumentsToSerialize) {
		type=TYPE_CALL;
		this.iface=iface;
		this.callbackId=callId;
		this.methodPrototype=methodPrototype;
		this.argumentsToSerialize=argumentsToSerialize;
	}
	public RemoteMessageObject(int type, int callbackId, Object ret) {
		this.type=type;
		this.callbackId=callbackId;
		this.ret=ret;
	}
	public RemoteMessageObject(int type, int callbackId) {
		this.type=type;
		this.callbackId=callbackId;
	}
	@Override
	public String toString() {
		StringBuilder ret=new StringBuilder();
		switch(type)
		{
		case TYPE_CALL:
			ret.append("call: "+iface+"."+methodPrototype+"[");
			if(argumentsToSerialize!=null)
			{
				boolean first=true;
				for(Object o: argumentsToSerialize)
				{
					if(!first)
					{
						ret.append(", ");
					}
					first=false;
					ret.append(""+o);
				}
			}
			ret.append("]");
			break;
		case TYPE_CALL_CALLBACK:
			ret.append("callback: "+callbackId+" ret: '"+this.ret+"'");
			break;
		case TYPE_RETURN:
			ret.append("return: "+this.ret);
			break;
		case TYPE_DISPOSE_CALLBACK:
			ret.append("disposeCallback: "+this.callbackId);
			break;
		default:
			ret.append(type);
			break;
		}
		return ret.toString();
	}
	public void setAsBinary(byte[] asBinary) {
		this.asBinary = asBinary;
	}
	public byte[] getClientSideAsBinary() {
		return asBinary;
	}
}
