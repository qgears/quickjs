package hu.qgears.quickjs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.quickjs.qpage.IQDisposableContainer;
import hu.qgears.quickjs.qpage.QComponent;

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
	private QComponent page;
	private IQDisposableContainer disposableContainer;
	
	public FrameLimiter(QComponent component, long minPeriodMillis) {
		super();
		this.page = component;
		this.disposableContainer=component;
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
				if(elapsed<-2*minPeriodMillis||elapsed>=minPeriodMillis)
				{
					page.getPageContainer().submitToUI(this::run);
				}else
				{
					long delay=minPeriodMillis-Math.max(elapsed, 0);
					page.getPageContainer().scheduleToUI(delay, this::run);
				}
			}
		}
	}
	private long prev=0;
	final public void run()
	{
		synchronized (this) {
			long t=System.currentTimeMillis();
			prev=t;

			
			await=false;
			tPrev=System.currentTimeMillis();
		}
		try {
			doExec();
		} catch (Exception e) {
			log.error("Error executing frame limited task.", e);
		}
	}
	public FrameLimiter setDisposableContainer(IQDisposableContainer disposableContainer) {
		this.disposableContainer = disposableContainer;
		return this;
	}
	protected abstract void doExec();
	public NoExceptionAutoClosable listenProperty(UtilListenableProperty<?> prop) {
		NoExceptionAutoClosable unreg=prop.addListenerWithInitialTrigger(e->executeTask());
		disposableContainer.addCloseable(unreg);
		return unreg;
	}
}
