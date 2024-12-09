package hu.qgears.quickjs.serialization;

public interface IRemotingCallbackHost {
	<T> void callback(int index, T value);
}
