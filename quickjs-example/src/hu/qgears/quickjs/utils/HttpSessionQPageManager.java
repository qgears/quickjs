package hu.qgears.quickjs.utils;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import hu.qgears.quickjs.qpage.QPageManager;

public class HttpSessionQPageManager {
	private static Object syncObject=new Object();
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

}
