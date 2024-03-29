package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamSupplier {
	InputStream open() throws IOException;
	default public String getCompressionMethod() {return null;}
	default Long getMaxAgeSeconds() {return null;} 
}
