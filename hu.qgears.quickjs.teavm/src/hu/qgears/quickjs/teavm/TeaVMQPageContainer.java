package hu.qgears.quickjs.teavm;

import org.teavm.jso.JSObject;

/** Java->JS calls that let QuickJS run in the browser. */
public interface TeaVMQPageContainer extends JSObject {
	/// Request that Java->JS messages are processed in JS. Does not process them at once but on a callback just after the
	/// Java code returns. (Implemented as a setTimeout(0)
	void requestProcessMessages();
	void javaMessageBegin();
	void javaMessageArgString(String s);
	void javaMessageArgBytes(byte[] bs);
	/// Send a Java message to JS.
	/// The same message that is send as a WebSocket message in case of server side execution of the code.
	/// @param header always "js" - different messages may be implemented later
	/// @param args args[0] is the JavaScript content that will be executed using eval() following args are additional arguments
	void javaMessage(String header);
	/// Get the initial context object in serialized format.
	byte[] getContextObjectSerialized();
	/// Send Remote call serialized to a byte array
	void sendRemoteCall(byte[] payload);
	/// Get number of replay objects (replicating server initialization)
	int getNReplayObject();
	/// Get number of replay objects (replicating server initialization)
	byte[] getReplayObject(int index);
	/// Open server connection and request callback when communication channel was opened.
	void requestCommunicationCallback();
}
