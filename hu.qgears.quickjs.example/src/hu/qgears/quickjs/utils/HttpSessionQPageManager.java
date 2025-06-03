package hu.qgears.quickjs.utils;


import hu.qgears.quickjs.qpage.QPageManager;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

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
			}
			
			@Override
			public void sessionCreated(HttpSessionEvent se) {
			}
		};
	}

}
