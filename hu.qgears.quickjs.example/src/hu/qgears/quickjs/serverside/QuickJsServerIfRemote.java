package hu.qgears.quickjs.serverside;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.quickjs.serverif.IQuickJsServerIfRemote;
import hu.qgears.quickjs.utils.gdpr.GdprSession;

public class QuickJsServerIfRemote implements IQuickJsServerIfRemote {
	private static Logger log = LoggerFactory.getLogger(QuickJsServerIfRemote.class);
	private GdprSession session;

	public QuickJsServerIfRemote(GdprSession session) {
		super();
		this.session = session;
	}

	@Override
	public String signalCookieAcceptedForSession() {
		log.info("Cookie accepted: "+session.getId());
		session.setCookieAccepted();
		return session.getId();
	}
}
