package hu.qgears.quickjs.serialization;

import hu.qgears.quickjs.helpers.PromiseImpl;

public interface IRemotingImplementation {
	<T> void executeRemoteCall(Class<T> returnType, PromiseImpl<T> remotingReturnValue, String methodPrototype,
			Object[] argumentsToSerialize);
}
