package hu.qgears.quickjs.utils.gdpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.MultiMapTreeImpl;
import hu.qgears.commons.UtilString;
import hu.qgears.quickjs.qpage.QPageContainer;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

public class GdprSessionIdManager
{
	/// In case cookies are not accepted then session is restricted to opening a qpage websocket which is almost instantenous.
	/// And the session is timed out once the qpage is timed out.
	/// When the QPage maintains its own active state by ping-pong then the session also has to be reinitialized
	private int maxInactiveIntervalCookiesNotAccepted=QPageContainer.getMinimalSessionTimeoutMs();
	/// ~1 year
	private int maxInactiveIntervalCookiesAccepted=365*24*60*60;
	private Random random;
	private List<HttpSessionListener> listeners=new ArrayList<>();
	private ISessionLoader sessionLoader;
	/// Session by timeout
	private MultiMapTreeImpl<Long, GdprSession> sessionTimeouts=new MultiMapTreeImpl<Long, GdprSession>();
	private HttpSessionListener[] readOnlyCopy=null;

	public GdprSessionIdManager(Server s, Random secureRandom, Timer timerToUsePurgingStaleSessions) {
		this.random=secureRandom;
		timerToUsePurgingStaleSessions.schedule(new TimerTask() {
			@Override
			public void run() {
				disposeStaleSessions();
			}
		}, 10000, 10000);
	}
	private Map<String, GdprSession> sessionsBySecureIdentifier=new HashMap<>();
	public GdprSession getSession(String sessionIdFromRequest, boolean cookiesAccepted) {
		long t=System.currentTimeMillis();
		GdprSession session;
		synchronized (sessionsBySecureIdentifier) {
			session=sessionsBySecureIdentifier.get(sessionIdFromRequest);
			if(session==null && sessionLoader!=null)
			{
				try {
					session=sessionLoader.loadSession(this, sessionIdFromRequest);
				} catch (Exception e) {
					log.error("Load session by id", e);
				}
			}
			if(session!=null)
			{
				// Update last access time at once to avoid race condition dispose of the session once we got it out of the storage.
				session.updateLastAccessedTime(t);
				if(cookiesAccepted && session.getMaxInactiveInterval()!=maxInactiveIntervalCookiesAccepted)
				{
					session.setMaxInactiveInterval(maxInactiveIntervalCookiesAccepted);
				}
			}
		}
		if(session==null)
		{
			String id=createNewId();
			session=createSessionForId(id, t, cookiesAccepted);
		}else
		{
			updateTimeout(session);
		}
		return session;
	}
	public GdprSession createSessionForId(String id, long t, boolean cookiesAccepted)
	{
		GdprSession session=new GdprSession(this, t, id);
		synchronized (sessionsBySecureIdentifier) {
			boolean exists=sessionsBySecureIdentifier.containsKey(id);
			if(exists)
			{
				throw new RuntimeException("Internal error - session id gen");
			}
			sessionsBySecureIdentifier.put(id, session);
		}
		session.setMaxInactiveInterval(cookiesAccepted?maxInactiveIntervalCookiesAccepted:maxInactiveIntervalCookiesNotAccepted);
		updateTimeout(session);
		HttpSessionEvent ev=new HttpSessionEvent(session);
		for(HttpSessionListener l: getCurrentListeners())
		{
			l.sessionCreated(ev);
		}
		return session;
	}
	private String createNewId() {
		byte[] bytes=new byte[512/8];	// 512 bits
		random.nextBytes(bytes);
		String ret=UtilString.toHex(bytes);
		return ret;
	}
	public void addEventListener(HttpSessionListener createSessionListener) {
		synchronized (listeners) {
			listeners.add(createSessionListener);
			readOnlyCopy=null;
		}
	}
	private HttpSessionListener[] getCurrentListeners()
	{
		synchronized (listeners) {
			if(readOnlyCopy==null)
			{
				readOnlyCopy=new HttpSessionListener[listeners.size()];
				for(int i=0;i<readOnlyCopy.length;++i)
				{
					readOnlyCopy[i] = listeners.get(i);
				}
			}
			return readOnlyCopy;
		}
	}
	protected void updateTimeout(GdprSession simpleSession)
	{
		long timeoutAtMilli=simpleSession.getMaxInactiveInterval()*1000+simpleSession.getLastAccessedTime();
		synchronized (sessionTimeouts) {
			if(null!=simpleSession.currentTimeoutRegistered)
			{
				sessionTimeouts.removeSingle(simpleSession.currentTimeoutRegistered, simpleSession);
			}
			simpleSession.currentTimeoutRegistered=timeoutAtMilli;
			sessionTimeouts.putSingle(simpleSession.currentTimeoutRegistered, simpleSession);
		}
	}
	private  Logger log=LoggerFactory.getLogger(getClass());
	public GdprSessionHandler handler;
	/**
	 * 
	 * @param simpleSession
	 * @param causeIsTimeout in case cause is timeout then timeout is re-checked and in case it was refreshed then dispose is cancelled.
	 *                   (this is necessary to avoid race conditon of a new access and disposing)
	 * @param currentTimeMs current time ms
	 */
	public void dispose(GdprSession simpleSession, boolean causeIsTimeout, long currentTimeMs) {
		synchronized (sessionsBySecureIdentifier) {
			if(causeIsTimeout && simpleSession.getRequiredDisposeTimeAt()>currentTimeMs)
			{
				return;
			}
			GdprSession prev=sessionsBySecureIdentifier.remove(simpleSession.getId());
			if(prev!=simpleSession)
			{
				log.error("dispose check fails, session removed does not equal this - should be impossible");
			}
		}
		synchronized (sessionTimeouts) {
			if(null!=simpleSession.currentTimeoutRegistered)
			{
				sessionTimeouts.removeSingle(simpleSession.currentTimeoutRegistered, simpleSession);
			}
			simpleSession.currentTimeoutRegistered=null;
		}
		HttpSessionEvent ev=new HttpSessionEvent(simpleSession);
		for(HttpSessionListener l: getCurrentListeners())
		{
			l.sessionDestroyed(ev);
		}
	}
	private void disposeStaleSessions() {
		try {
			long t=System.currentTimeMillis();
			List<GdprSession> toDispose=new ArrayList<>();
			synchronized (sessionTimeouts) {
				long v=t+1;
				if(sessionTimeouts.size()>0)
				{
					v=sessionTimeouts.firstKey();
				}
				while(v<= t)
				{
					toDispose.addAll(sessionTimeouts.remove(v));
					v=t+1;
					if(sessionTimeouts.size()>0)
					{
						v=sessionTimeouts.firstKey();
					}
				}
			}
			for(GdprSession sess: toDispose)
			{
				try {
					dispose(sess, true, t);
				} catch (Exception e) {
					log.error("disposeStaleSessions - dispose one", e);
				}
			}
		} catch (Exception e) {
			log.error("disposeStaleSessions", e);
		}
	}
	public String getAcceptCookiesCookieName() {
		return handler.cookiesAcceptedCookieName;
	}
	public String getSessionIdCookieName() {
		return handler.sessionCookieName;
	}
	public void setCookieAccepted(GdprSession session) {
		if(session.getMaxInactiveInterval()!=maxInactiveIntervalCookiesAccepted)
		{
			session.setMaxInactiveInterval(maxInactiveIntervalCookiesAccepted);
		}	
	}
	public void setSessionLoader(ISessionLoader sessionLoader) {
		this.sessionLoader = sessionLoader;
	}
}
