package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read HTTP headers and restore the context in which the application is accessed.
 * In case the application is accessed through a proxy then this to work requires correct setup in the proxy:
 * Headers required:
 *  "X-Forwarded-Server" - the public name of the server
 *  "X-Forwarded-Proto" - the public access protocol - probably https
 *  "X-Forwarded-Port" - the public assess port - null means the default for the protocol
 *  "X-Forwarded-Context" - the "folder" in which this application is running. With starting '/' and without ending '/'
 */
public class UtilHttpContext {
	static Logger log=LoggerFactory.getLogger(UtilHttpContext.class);
	/**
	 * Header name through which the original server name is sent to proxied servers (login, auth, content)
	 * In case of server side override the same key is used to write a value to the attributes of the request.
	 */
	public static final String forwardedServerName="X-Forwarded-Server";

	public static String getServer(Request baseRequest)
	{
		Object attributeOverride=baseRequest.getAttribute(forwardedServerName);
		if(attributeOverride instanceof String)
		{
			return (String)attributeOverride;
		}
		return getHeader(baseRequest, forwardedServerName);
	}
	public static StringBuilder getServerUrl(Request baseRequest)
	{
		String srv=getServer(baseRequest);
		if(srv==null)
		{
			srv=baseRequest.getServerName();
		}
		String proto=getHeader(baseRequest, "X-Forwarded-Proto");
		if(proto==null)
		{
			proto=baseRequest.getScheme();
		}
		StringBuilder url=new StringBuilder();
		url.append(proto);
		url.append("://");
		url.append(srv);
		String port=getHeader(baseRequest, "X-Forwarded-Port");
		if(port==null)
		{
			port=""+baseRequest.getServerPort();
		}
		boolean defaultport=false;
		switch(proto)
		{
		case "http":
			defaultport=port.equals("80");
		case "https":
			defaultport=port.equals("443");
		}
		if(!defaultport)
		{
			url.append(":");
			url.append(port);
		}
		return url;
	}
	public static StringBuilder getRootURL(Request baseRequest)
	{
		StringBuilder url=getServerUrl(baseRequest);
		String context=getHeader(baseRequest, "X-Forwarded-Context");
		if(context!=null)
		{
			url.append(context);
		}
		return url;
	}
	public static void sendRedirect(Request baseRequest, HttpServletResponse response, String string) throws IOException {
		StringBuilder url=getRootURL(baseRequest);
		url.append(baseRequest.getContextPath());
		url.append(string);
		log.info("Redirect to: "+url.toString());
		response.sendRedirect(url.toString());
	}
	/**
	 * Get the rewrite context of this page (if a reverse proxy rewrite is present)
	 * @param baseRequest
	 * @return never ends with / Will be '' if no reverse proxy is present. 'path' when reverse proxy is present
	 */
	public static String getContext(Request baseRequest)
	{
		String context=getHeader(baseRequest, "X-Forwarded-Context");
		if(context!=null)
		{
			return context;
		}
		return "";
	}
	/**
	 * Get the first header by name if exists.
	 * @param baseRequest
	 * @param string
	 * @return
	 */
	public static String getHeader(Request baseRequest, String string) {
		Enumeration<String> hs=baseRequest.getHeaders(string);
		if(hs.hasMoreElements())
		{
			return hs.nextElement();
		}
		return null;
	}
	/**
	 * Magic cookie date that is tha latest possible value that is correctly handled by all browsers.
	 * According to Internet myths.
	 * @return
	 */
	public static String getNeverExpiresCookieDate() {
		return "Tue, 19 Jan 2038 03:14:07 GMT";
	}

}
