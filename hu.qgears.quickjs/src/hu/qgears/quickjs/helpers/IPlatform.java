package hu.qgears.quickjs.helpers;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.qpage.EQPageMode;
import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.qpage.IndexedComm;
import hu.qgears.quickjs.serialization.SerializeBase;

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

	/** Check whether we are on QPage thread or not.
	 * @return
	 */
	boolean isQPageThread();

	/** Internal API.
	 * Can be used in the very rare case when for some reason the page is blocked for a time.
	 * Calling this periodically will disable page disposal by timer.
	 * Dispose timer is only implemented on the server side. In the browser this is NOOP.
	 */
	void reinitDisposeTimer();
	/** Page dispose timeout. Should only be used in server side mode.
	 * @return 0 means no timeout
	 */
	int getTIMEOUT_DISPOSE_MS();

	/**
	 * Start communication with JS code.
	 * In case of server side this opens an {@link IndexedComm} stream over WebSocket and registers to application to the session object.
	 * In case of client side message queues must be implemented TODO
	 */
	void startCommunicationWithJs();

	void disposeCommunicationToJS();

	void submitToUI(Runnable r);

	<V> Promise<V> submitToUICallable(Callable<V> c);

	/** Remove the page from the pages registry. Only useful on server */
	void deregister();

	EQPageMode getMode();

	void writePreloadHeaders(HtmlTemplate parent);

	void writeHeaders(HtmlTemplate parent);

	List<String> getJsOrder();

	String loadResource(String fname) throws Exception;

	void configureJsGlobalQPage(HtmlTemplate parent, String string);
	
	/** Get the data serializator - used in case of Client side or hybrid mode */
	SerializeBase getSerializator();

	/** Write a blob object into a HTML JS stream. It is a platform method because sadly java.uitl.Base64 is not present in TeaVM. */
	void writeBlobObject(HtmlTemplate htmlTemplate, byte[] o);
	void writeBlobObject(HtmlTemplate htmlTemplate, byte[] o, int pos, int length);
	/** Is this instance running on the server?
	 * @return
	 */
	boolean isServer();
	/** In case of hybrid mode the server side processing messages are stored in this list.
	 * @return
	 */
	List<byte[]> getReplayObjects();

	void setSetupContext(Supplier<NoExceptionAutoClosable> setupContext);
}
