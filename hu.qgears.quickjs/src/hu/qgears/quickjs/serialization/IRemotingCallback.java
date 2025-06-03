package hu.qgears.quickjs.serialization;

/**
 * Marker interface that the parameter need not be serialized but an index has to be passed instead.
 * @param <T>
 */
public interface IRemotingCallback <T> {
	void callback(T value);
}
