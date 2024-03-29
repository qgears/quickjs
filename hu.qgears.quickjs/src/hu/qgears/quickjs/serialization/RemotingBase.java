package hu.qgears.quickjs.serialization;

import hu.qgears.quickjs.helpers.PromiseImpl;

abstract public class RemotingBase implements IRemotingBase {
	private IRemotingImplementation remotingImplementation;
	protected <T> void executeRemoteCall(Class<T> returnType, PromiseImpl<T> remotingReturnValue, String methodPrototype,
			Object[] argumentsToSerialize) {
		remotingImplementation.executeRemoteCall(returnType, remotingReturnValue, methodPrototype, argumentsToSerialize);
	}
	public void setRemotingImplementation(IRemotingImplementation remotingImplementation) {
		this.remotingImplementation = remotingImplementation;
	}
}
