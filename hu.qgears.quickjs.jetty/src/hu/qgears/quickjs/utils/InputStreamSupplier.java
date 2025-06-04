package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamSupplier {
	InputStream open() throws IOException;
	default public String getCompressionMethod() {return null;}
	default Long getMaxAgeSeconds() {return null;}
	default public boolean supportsRange() {return false;}
	/** Open input to read a range of it.
	 * The stream has to be closed by the caller.
	 * @param offsetFrom offset to read from (including)
	 * @param offsetTo offset to read to (excluding, offsetTo>offsetFrom)
	 * @return null means not implemented
	 * @throws IOException
	 */
	default public InputStream openRange(long offsetFrom, long offsetTo) throws IOException {
		if(offsetFrom==0)
		{
			return open();
		}
		throw new IOException("openRange offsetFrom!=0 not supported.");
	}
	default boolean supportsLength() {return false;}
	default long length() {throw new UnsupportedOperationException();}
}
