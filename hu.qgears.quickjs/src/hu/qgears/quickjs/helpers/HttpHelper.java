package hu.qgears.quickjs.helpers;

public class HttpHelper {
	/**
	 * Magic cookie date that is tha latest possible value that is correctly handled by all browsers.
	 * According to Internet myths.
	 * @return
	 */
	public static String getNeverExpiresCookieDate() {
		return "Tue, 19 Jan 2038 03:14:07 GMT";
	}
}
