package hu.qgears.quickjs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.quickjs.qpage.QPage;

/**
 * Connect a real time event to the UI:
 *  * Frequency of the source event can be any high.
 *  * The UI update is executed by a limit delay at most.
 */
abstract public class FrameLimiter {
	private Logger log=LoggerFactory.getLogger(getClass());
	private long minPeriodMillis;
	private long tPrev;
	private boolean await=false;
	private QPage page;
	
	public FrameLimiter(QPage page, long minPeriodMillis) {
		super();
		this.page = page;
		this.minPeriodMillis = minPeriodMillis;
	}
	public void executeTask()
	{
		synchronized (this) {
			if(!await)
			{
				await=true;
				long now=System.currentTimeMillis();
				long elapsed=now-tPrev;
				if(elapsed<0||elapsed>=minPeriodMillis)
				{
					page.submitToUI(this::run);
				}else
				{
					long delay=minPeriodMillis-elapsed;
					page.scheduleToUI(delay, this::run);
				}
			}
		}
	}
	final public void run()
	{
		synchronized (this) {
			await=false;
			tPrev=System.currentTimeMillis();
		}
		try {
			doExec();
		} catch (Exception e) {
			log.error("Error executing frame limited task.", e);
		}
	}
	protected abstract void doExec();
	public FrameLimiter listenProperty(UtilListenableProperty<?> prop) {
		hu.qgears.commons.NoExceptionAutoClosable unreg=prop.addListenerWithInitialTrigger(e->executeTask());
		page.disposedEvent.addOnReadyHandler(e->{unreg.close();});
		return this;
	}
}
