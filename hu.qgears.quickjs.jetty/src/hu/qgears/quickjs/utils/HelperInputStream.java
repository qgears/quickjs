package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HelperInputStream {
	private static Method methodSkipNBytes;
	static {
		Class<InputStream> c=InputStream.class;
		try {
			methodSkipNBytes=c.getMethod("skipNBytes", long.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	/** Safe accessor of InputStream.skipNBytes also before JRE v12
	 * 
	 * @param fis
	 * @param offsetFrom
	 * @throws IOException 
	 */
	public static void skipNBytes(InputStream fis, long offsetFrom) throws IOException {
		if(methodSkipNBytes!=null)
		{
			try {
				methodSkipNBytes.invoke(fis, offsetFrom);
			} catch (IllegalAccessException e) {
				throw new IOException("skipNBytes invocation error", e);
			} catch (InvocationTargetException e) {
				throw new IOException("skipNBytes invocation error", e);
			}
		}else
		{
			long remaining=offsetFrom;
			while(remaining>0)
			{
				long skipped=fis.skip(remaining);
				remaining-=skipped;
				if(skipped==0)
				{
					throw new IOException("Maybe EOF");
				}
			}
		}
	}
}
