package hu.qgears.quickjs.qpage;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;

public class UtilEventWithListenerTrack<T> extends UtilEvent<T> {
	private UtilEventListener<UtilEventWithListenerTrack<T>> nListenerChanged;
	public UtilEventWithListenerTrack(UtilEventListener<UtilEventWithListenerTrack<T>> nListenerChanged)
	{
		this.nListenerChanged=nListenerChanged;
	}
	@Override
	public void addListener(UtilEventListener<T> l) {
		super.addListener(l);
		nListenerChanged.eventHappened(this);
	}
	@Override
	public void removeListener(UtilEventListener<T> l) {
		super.removeListener(l);
		nListenerChanged.eventHappened(this);
	}
}
