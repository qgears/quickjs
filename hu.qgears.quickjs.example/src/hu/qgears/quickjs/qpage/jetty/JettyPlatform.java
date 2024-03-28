package hu.qgears.quickjs.qpage.jetty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.SafeTimerTask;
import hu.qgears.commons.UtilTimer;
import hu.qgears.quickjs.helpers.IPlatform;
import hu.qgears.quickjs.helpers.QTimer;
import hu.qgears.quickjs.qpage.QPageContainer;

public class JettyPlatform implements IPlatform {
	private static final Logger log=LoggerFactory.getLogger(JettyPlatform.class);
	QPageContainer pageContainer;
	public JettyPlatform(QPageContainer pageContainer) {
		this.pageContainer=pageContainer;
	}
	private class JettyQTimer extends QTimer
	{
		SafeTimerTask stt;
		@Override
		public void close() {
			stt.cancel();
			super.close();
		}
	}
	@Override
	public QTimer startTimer(Runnable r, int firstTimeoutMs, int periodMs) {
		JettyQTimer t=new JettyQTimer();
		t.stt=new SafeTimerTask() {
			@Override
			protected void doRun() {
				pageContainer.submitTimerTask(t, r);
				pageContainer.submitToUI(()->{
					try {
						if(!isCancelled())
						{
							r.run();
						}
					} catch (Exception e) {
						log.error("Unhandled exception in timer", e);
					}
				});
			}
		};
		UtilTimer.javaTimer.schedule(t.stt, firstTimeoutMs, periodMs);
		return t;
	}
}
