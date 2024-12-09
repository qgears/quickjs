package hu.qgears.quickjs.serialization;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.helpers.PromiseImpl;

/**
 * Repesents a callback object on the client side that can be used to close the callback mechanism.
 */
public class CallbackRegistryEntry implements NoExceptionAutoClosable {
	public final int id;
	public final IRemotingCallback<?> callback;
	Object[] argumentsToSerialize;
	public final Promise<CallbackRegistryEntry> closed=new PromiseImpl<>();
	public byte[] payload;
	public CallbackRegistryEntry(int nextCallbackRegistryIndex, String methodPrototype, IRemotingCallback<?> callback) {
		id=nextCallbackRegistryIndex;
		this.callback=callback;
	}
	public void setArgs(Object[] argumentsToSerialize) {
		this.argumentsToSerialize=argumentsToSerialize;
	}
	@Override
	public void close() {
		((PromiseImpl<CallbackRegistryEntry>)closed).ready(this);
	}
}
