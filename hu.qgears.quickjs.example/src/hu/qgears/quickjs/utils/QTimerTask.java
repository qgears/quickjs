package hu.qgears.quickjs.utils;

import hu.qgears.commons.SafeTimerTask;

/**
 * TimerTask wrapper that can be initialized with a lambda expression. (Functional interface)
 * Also wraps exceptions and logs to slf4j logger.
 */
public class QTimerTask extends SafeTimerTask
{
	private Runnable runnable;
	public QTimerTask(Runnable runnable) {
		super();
		this.runnable = runnable;
	}
	@Override
	public void doRun() {
		runnable.run();
	}
}
