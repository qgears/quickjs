package hu.qgears.quickjs.qpage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import hu.qgears.commons.UtilTimer;

/**
 * The session object in terms of QPage.
 */
public class QPageManager {
	public QPageManager() {
	}
	private Map<String, QPageContainer> pages=new HashMap<>();
	private Map<String, Object> userdata=new HashMap<>();
	public static Timer disposeTimer=UtilTimer.javaTimer;
	private int idCtr=0;
	/**
	 * Salt is used to separate page ids in case of the server is restarted but sessions are
	 * persistent between server restarts. (It may be a possible but rare configuration.)
	 * It does not help from attacks like stealing of sessions. The session manager must do that task.
	 */
	private String salt=""+System.currentTimeMillis();
	public QPageContainer getPage(String id) {
		synchronized (pages) {
			return pages.get(id); 
		}
	}
	public String createId() {
		synchronized (pages) {
			return salt+"_"+idCtr++;
		}
	}
	public void register(String identifier, QPageContainer qPage) {
		synchronized (pages) {
			pages.put(identifier, qPage);
		}
	}
	public void dispose() {
		List<QPageContainer> toDispose;
		synchronized (pages) {
			toDispose=new ArrayList<>(pages.values());
			pages.clear();
		}
		for(QPageContainer p: toDispose)
		{
			p.dispose();
		}
	}
	public void remove(QPageContainer qPage) {
		synchronized (pages) {
			pages.remove(qPage.getId());
		}
	}
	public void setUserData(String key, Object o) {
		synchronized (userdata) {
			userdata.put(key, o);
		}
	}
	public Object getUserData(String string) {
		synchronized (userdata) {
			return userdata.get(string);
		}
	}
}
