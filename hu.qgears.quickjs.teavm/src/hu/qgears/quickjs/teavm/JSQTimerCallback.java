package hu.qgears.quickjs.teavm;

import org.teavm.jso.JSObject;

public interface JSQTimerCallback extends JSObject {
	void timeout();
}
