package hu.qgears.quickjs.teavm;

import org.teavm.jso.JSObject;

public interface JSQPageCallback extends JSObject {
	void createPageContainer(String identifier, int mode);
	void openPath(String path);
	void msgHeader(String header);
	void processMessages();
	/** Communication channel to server has been opened. */
	void channelOpened();
	void channelReady();
	void messageReceived(byte[] data);
	void channelError();
	void channelClosed();
}
