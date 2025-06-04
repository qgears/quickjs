package hu.qgears.quickjs.utils.gdpr;

public interface ISessionLoader {
	GdprSession loadSession(GdprSessionIdManager manager, String sessionIdFromRequest) throws Exception;
}
