package hu.qgears.quickjs.utils;

import org.eclipse.jetty.util.UrlEncoded;

import jakarta.servlet.http.HttpServletResponse;

public class UtilJetty {
	public static final Long maxAgeSeconds_year=60l*60l*24l*366l;
	/**
	 * Set response header that it is cacheable until 1 day.
	 * @param response
	 */
	public static void setResponseCacheable(HttpServletResponse response) {
		setResponseCacheable(response, 86400);
	}
	/**
	 * Set response header that it is cacheable until maxAgeSeconds timeout.
	 * @param response
	 * @param maxAgeSeconds maximum age in seconds
	 */
	public static void setResponseCacheable(HttpServletResponse response, long maxAgeSeconds) {
		response.setHeader("Cache-Control", "public, max-age="+maxAgeSeconds+", immutable");
	}
	/**
	 * Set response header that it is not cacheable at all.
	 * @param response
	 */
	public static void setResponseNotCacheable(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store");
	}
	public static String encodeUrl(String string) {
		String ret=UrlEncoded.encodeString(string);
		String ret2=ret.replaceAll("\\+", "%20");
		return ret2;
	}
}
