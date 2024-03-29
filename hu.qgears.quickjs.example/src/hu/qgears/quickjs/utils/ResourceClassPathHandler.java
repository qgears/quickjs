package hu.qgears.quickjs.utils;

import java.net.URL;

/**
 * Handle resources from Java classpath.
 */
public class ResourceClassPathHandler extends AbstractResourceHandler {
	protected Class<?> clazz;
	public ResourceClassPathHandler(Class<?> clazz) {
		super();
		this.clazz=clazz;
	}
	@Override
	protected InputStreamSupplier getFileOpener(String resname, String acceptedEncoding) {
		URL url=clazz.getResource(resname);
		if(url!=null)
		{
			return ()->url.openStream();
		}else
		{
			return null;
		}
	}
}
