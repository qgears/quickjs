package hu.qgears.quickjs.qpage;

import java.util.Map;

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
