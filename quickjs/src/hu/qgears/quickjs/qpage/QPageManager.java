package hu.qgears.quickjs.qpage;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.jetty.server.Request;

public class QPageManager {
	private static Object syncObject=new Object();
	private Map<String, QPage> pages=new HashMap<>();
	public static Timer disposeTimer=new Timer("QPage dispose timer");
	private int idCtr=0;
	/**
	 * Salt is used to separate page ids in case of the server is restarted but sessions are
	 * persistent between server restarts. (It may be a possible but rare configuration.)
	 * It does not help from attacks like stealing of sessions. The session manager must do that task.
	 */
	private String salt=""+System.currentTimeMillis();
	public static QPageManager getManager(HttpSession sess) {
		synchronized (syncObject) {
			QPageManager manager=(QPageManager)sess.getAttribute(QPageManager.class.getSimpleName());
			if(manager==null)
			{
				manager=new QPageManager();
				sess.setAttribute(QPageManager.class.getSimpleName(), manager);
			}
			return manager;
		}
	}
	public QPage getPage(Request baseRequest) {
		String id=baseRequest.getParameter(QPage.class.getSimpleName());
		synchronized (pages) {
			return pages.get(id); 
		}
	}
	public String createId() {
		synchronized (pages) {
			return salt+"_"+idCtr++;
		}
	}
	public void register(String identifier, QPage qPage) {
		synchronized (pages) {
			pages.put(identifier, qPage);
		}
	}
	public static HttpSessionListener createSessionListener()
	{
		return new HttpSessionListener() {
			
			@Override
			public void sessionDestroyed(HttpSessionEvent se) {
				QPageManager manager;
				synchronized (syncObject) {
					manager=(QPageManager)se.getSession().getAttribute(QPageManager.class.getSimpleName());
				}
				if(manager!=null)
				{
					manager.dispose();
				}
				System.out.println("Session diposed!");
			}
			
			@Override
			public void sessionCreated(HttpSessionEvent se) {
				System.out.println("Session created!");
			}
		};
	}
	protected void dispose() {
		for(QPage p: pages.values())
		{
			p.dispose();
		}
		pages.clear();
	}
}
