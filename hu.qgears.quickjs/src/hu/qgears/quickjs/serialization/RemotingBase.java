package hu.qgears.quickjs.serialization;

import hu.qgears.quickjs.helpers.PromiseImpl;

abstract public class RemotingBase implements IRemotingBase {
	private IRemotingImplementation remotingImplementation;
	protected <T> void executeRemoteCall(Class<T> returnType, PromiseImpl<T> remotingReturnValue, String iface, String methodPrototype,
			Object[] argumentsToSerialize, boolean isRegisterCallback) {
		remotingImplementation.executeRemoteCall(returnType, remotingReturnValue, iface, methodPrototype, argumentsToSerialize, isRegisterCallback);
	}
	public void setRemotingImplementation(IRemotingImplementation remotingImplementation) {
		this.remotingImplementation = remotingImplementation;
	}
	protected CallbackRegistryEntry registerCallback(String methodPrototype, IRemotingCallback<?> arg0) {
		return remotingImplementation.registerCallback(methodPrototype, arg0);
	}
}
