package hu.qgears.quickjs.utils;

import org.eclipse.jetty.server.Request;

import hu.qgears.quickjs.qpage.QPage;

/**
 * Interface to communicate with test environment when auto-testing is used.
 */
public interface IQTestEnvironment {
	/**
	 * Signal that a page was instantiated. Called after the page initial state was sent to the http client.
	 * @param baseRequest
	 * @param newPage
	 */
	default void qPageCreated(Request baseRequest, QPage newPage) {}
}
