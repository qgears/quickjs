package hu.qgears.quickjs.qpage;

import java.util.Map;

/** Interface to be able to connect objects to the GUI elements.
 *  The storage is not thread safe should only be accesed form the UI thread. */
public interface IUserObjectStorage {
	Map<String, Object> getUserObjectStorage();
	default Object setUserObject(String key, Object value)
	{
		Map<String, Object> storage=getUserObjectStorage();
		synchronized (storage) {
			return getUserObjectStorage().put(key, value);
		}
	}
	default Object getUserObject(String key)
	{
		Map<String, Object> storage=getUserObjectStorage();
		synchronized (storage) {
			return getUserObjectStorage().get(key);
		}
	}
}
