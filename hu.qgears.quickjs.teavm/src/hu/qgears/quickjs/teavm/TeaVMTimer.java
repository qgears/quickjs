package hu.qgears.quickjs.teavm;

import org.teavm.jso.JSBody;

import hu.qgears.quickjs.helpers.QTimer;

public class TeaVMTimer extends QTimer implements JSQTimerCallback {
	Runnable r; int firstTimeoutMs; int periodMs;
	TeaVMGui gui;
	public TeaVMTimer(TeaVMGui gui, Runnable r, int firstTimeoutMs, int periodMs)
	{
		this.gui=gui;
		this.r=r;
		this.firstTimeoutMs=firstTimeoutMs;
		this.periodMs=periodMs;
		setTimeout(firstTimeoutMs);
	}
	TeaVMTimerJS current;
	@JSBody(params = { "callback", "ms" }, script = "return globalQPage.setTimeout(callback, ms)")
	public static native TeaVMTimerJS setTimeout(JSQTimerCallback callback, int ms);

	private void setTimeout(int ms) {
		cancelCurrent();
		current=setTimeout(this, ms);
	}
	private void cancelCurrent()
	{
		if(current!=null)
		{
			current.cancel();
			current=null;
		}
	}
	@Override
	public void close() {
		cancelCurrent();
		super.close();
	}

	@Override
	public void timeout() {
		current=null;
		if(isClosed())
		{
			return;
		}
		r.run();
		gui.evalCollectedJs();
		if(isClosed())
		{
			return;
		}
		setTimeout(periodMs);
	}
}
