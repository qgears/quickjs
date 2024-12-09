package hu.qgears.quickjs.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;

/** Remote (server side) stub of the callback (on the browser client) */
public class CallbackOnRemote<T> implements IRemotingCallback<T>{
	private IRemotingCallbackHost host;
	private int index;
	NoExceptionAutoClosable closeable;
	private static Logger log=LoggerFactory.getLogger(CallbackOnRemote.class);
	public CallbackOnRemote(int index) {
		this.index=index;
	}
	@Override
	public void callback(T value) {
		host.callback(index, value);
	}
	public void setHost(IRemotingCallbackHost host) {
		this.host=host;
	}
	@Override
	public String toString() {
		return "Callback index: "+index;
	}
	public void setCloseable(NoExceptionAutoClosable closeable) {
		this.closeable=closeable;
	}
	public int getIndex() {
		return index;
	}
	public void dispose() {
		try {
			if(closeable!=null)
			{
				closeable.close();
			}
		} catch (Exception e) {
			log.error("dispose", e);
		}
	}
}
