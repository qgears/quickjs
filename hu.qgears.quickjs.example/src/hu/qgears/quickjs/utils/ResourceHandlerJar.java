package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.commons.UtilFile;

/**
 * Handle resources from Java classpath.
 */
public class ResourceHandlerJar extends AbstractHandler {
	protected Class<?> clazz;
	protected Map<String, String> mimetypes=new HashMap<>();
	public ResourceHandlerJar(Class<?> clazz) {
		this.clazz=clazz;
		registerKnownMimeTypes();
	}
	/**
	 * Subclasses may override to remove known types.
	 */
	protected void registerKnownMimeTypes()
	{
		registerMimeType("js", "text/javascript");
		registerMimeType("css", "text/css");
		registerMimeType("svg", "image/svg+xml");
	}
	/**
	 * Register additional known mime type.
	 * @param extension
	 * @param mimeType
	 */
	public void registerMimeType(String extension, String mimeType)
	{
		mimetypes.put(extension, mimeType);
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		handle(new HttpPath(target), baseRequest, request, response);
	}
	protected void handle(HttpPath path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		validatepieces(path);
		String extension=path.getExtension();
		String resname=path.toStringPath();
		URL url=clazz.getResource(resname);
		if(url!=null)
		{
			baseRequest.setHandled(true);
			if(extension!=null)
			{
				String mime=mimetypes.get(extension);
				if(mime!=null)
				{
					response.setContentType(mime);
				}
			}
			byte[] data=UtilFile.loadFile(url);
			response.getOutputStream().write(data);
		}
	}

	public static void validatepieces(HttpPath pieces) {
		for(String s: pieces.getPieces())
		{
			if(s.equals(".") || s.equals(".."))
			{
				throw new IllegalArgumentException();
			}
			for(int i=0;i<s.length();++i)
			{
				char c=s.charAt(i);
				if(
					(c>='a' && c<='z')
					||
					(c>='A' && c<='Z')
					||
					(c>='0' && c<='9')
					||
					(c=='.' || c=='-' || c=='_')
					)
				{
					// Allowed letters
				}else
				{
					throw new IllegalArgumentException("Illegal letter in path: '"+c+"'");
				}
			}
		}
	}

}
