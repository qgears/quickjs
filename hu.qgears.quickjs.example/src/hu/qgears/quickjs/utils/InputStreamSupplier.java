package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamSupplier {
	InputStream open() throws IOException;
	default public String getCompressionMethod() {return null;}
	default Long getMaxAgeSeconds() {return null;}
	default public boolean supportsRange() {return false;}
	default public InputStream openRange(long n) throws IOException {
		if(n==0)
		{
			return open();
		}
		return null;}
	default boolean supportsLength() {return false;}
	default long length() {throw new UnsupportedOperationException();}
}
