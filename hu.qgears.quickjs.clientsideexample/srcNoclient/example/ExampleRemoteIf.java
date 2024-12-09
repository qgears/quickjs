package example;

import java.util.List;
import java.util.Map;
import java.util.Set;

import hu.qgears.quickjs.serialization.IQCallContext;
import hu.qgears.quickjs.serialization.IRemotingCallback;

public interface ExampleRemoteIf extends MyIface {
	List<String> findAll(String s);
	void masik(Set<Integer> myset);
	Map<String, Integer> harmadik(Set<Integer> myset, String kutyus);
	int alma(int c);
	ExampleSerializableObject testit();
	void mycallback(IQCallContext context, IRemotingCallback<String> callbak);
}
