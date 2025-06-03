package example;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import hu.qgears.commons.UtilTimer;
import hu.qgears.quickjs.serialization.IQCallContext;
import hu.qgears.quickjs.serialization.IRemotingCallback;

public class RemoteMock implements ExampleRemoteIf {
	public String almaKorte;
	@Override
	public int alma(String korte) {
		almaKorte=korte;
		return korte.length();
	}

	
	@Override
	public void mycallback(IQCallContext context, IRemotingCallback<String> callbak) {
		// System.out.println("Context: "+context);
		UtilTimer.javaTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				callbak.callback("EXECCALLBACK");
			}
		}, 100);
	}

	@Override
	public List<String> findAll(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void masik(Set<Integer> myset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Integer> harmadik(Set<Integer> myset, String kutyus) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int alma(int c) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ExampleSerializableObject testit() {
		// TODO Auto-generated method stub
		return null;
	}

}
