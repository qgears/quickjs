package hu.qgears.quickjs.utils;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.UrlEncoded;

public class UtilJetty {
	/**
	 * Set response header that it is cacheable until 1 day.
	 * @param response
	 */
	public static void setResponseCacheable(HttpServletResponse response) {
		response.setHeader("Cache-Control", "public, max-age=86400, immutable");
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
