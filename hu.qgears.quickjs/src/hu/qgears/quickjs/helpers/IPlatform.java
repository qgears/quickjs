package hu.qgears.quickjs.helpers;

/** The platform implements the platform-specific features: timers, callbacks etc.
 * The implementation is different on the client and on the http server and can be different on different types of http servers. */
public interface IPlatform {
	/**
	 * Start a timer on the platform.
	 * @param r
	 * @param firstTimeoutMs
	 * @param periodMs
	 * @return
	 */
	QTimer startTimer(Runnable r, int firstTimeoutMs, int periodMs);
}
