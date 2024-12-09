package hu.qgears.quickjs.serialization;

import hu.qgears.quickjs.helpers.PromiseImpl;

public interface IRemotingImplementation extends IRemotingCallbackHost {
	<T> void executeRemoteCall(Class<T> returnType, PromiseImpl<T> remotingReturnValue, String iface, String methodPrototype,
			Object[] argumentsToSerialize, boolean isRegisterCallback);

	CallbackRegistryEntry registerCallback(String methodPrototype, IRemotingCallback<?> arg0);
}
