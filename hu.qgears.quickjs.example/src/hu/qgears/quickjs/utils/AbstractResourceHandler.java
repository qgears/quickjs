package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.commons.UtilFile;

/**
 * Handle resources.
 */
public abstract class AbstractResourceHandler extends AbstractHandler {
	protected Map<String, Type> mimetypes=new HashMap<>();
	protected Set<String> disallowedFileTypes=new HashSet<>();
	public class Type
	{
		public final String mimeType;
		/** Should the server compress this type of files? */
		public final boolean compress;
		public Type(String mimeType, boolean compress) {
			super();
			this.mimeType = mimeType;
			this.compress=compress;
		}
	}
	public AbstractResourceHandler() {
		registerTypes();
	}
	protected void registerTypes() {
		registerMimeType("js", "text/javascript", true);
		registerMimeType("css", "text/css", true);
		registerMimeType("svg", "image/svg+xml", true);
		registerMimeType("jpg", "image/jpg", false);
		registerMimeType("png", "image/png", false);
		registerMimeType("html", "text/html", true);
		registerMimeType("webp", "image/webp", false);
		registerMimeType("doc", "application/msword", true);
		registerMimeType("docx", "application/vnd.openxmlformats", true);
		registerMimeType("ods", "application/vnd.oasis.opendocument.spreadsheet", false);
		registerMimeType("fods", "application/vnd.oasis.opendocument.spreadsheet-flat-xml", true);
		registerMimeType("pdf", "application/pdf", true);
		registerMimeType("md", "text/plain", true);
		registerMimeType("txt", "text/plain", true);
		registerMimeType("json", "text/json", true);
		registerMimeType("woff", "font/woff", true);
		disallowedFileTypes.add("class");
		disallowedFileTypes.add("java");
		disallowedFileTypes.add("gitignore");
	}
	private void registerMimeType(String extension, String mimeType, boolean compress) {
		mimetypes.put(extension, new Type(mimeType, compress));
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		handle(new HttpPath(target), baseRequest, request, response);
	}
	private void handle(HttpPath path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		validatepieces(path);
		String extension=path.getExtension();
		validateExtension(extension);
		String resname=path.toStringPath();
		String mime=getMimeType(extension);
		String acceptEncoding=baseRequest.getHeader("Accept-Encoding");
		InputStreamSupplier fileOpener=getFileOpener(resname, acceptEncoding);
		if(fileOpener!=null)
		{
			String encoding=fileOpener.getCompressionMethod();
			if(encoding!=null)
			{
				response.setHeader("Content-Encoding", encoding);
			}
			if(fileOpener.getMaxAgeSeconds()!=null)
			{
				UtilJetty.setResponseCacheable(response, fileOpener.getMaxAgeSeconds());
			}
			baseRequest.setHandled(true);
			if(extension!=null)
			{
				if(mime!=null)
				{
					response.setContentType(mime);
					byte[] data=UtilFile.loadFile(fileOpener.open());
					response.getOutputStream().write(data);
				}else
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File type not handled: "+extension);
				}
			}else
			{
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File type not found: "+extension);
			}
		}
	}
	protected abstract InputStreamSupplier getFileOpener(String resname, String acceptEncoding);
	public void validateExtension(String ext)
	{
		if(disallowedFileTypes.contains(ext))
		{
			throw new IllegalArgumentException("No such file");
		}
	}
	public String getMimeType(String extension) {
		Type t=mimetypes.get(extension);
		return t==null?null:t.mimeType;
	}
	public Type getType(String extension) {
		Type t=mimetypes.get(extension);
		return t;
	}
	public String getMimeTypeSafe(String extension) {
		String ret=getMimeType(extension);
		if(ret==null)
		{
			ret="application/binary";
		}
		return ret;
	}
	public static void validatepieces(HttpPath pieces) {
		if(pieces.getPieces().size()==0)
		{
			throw new IllegalArgumentException("folder listing not available");
		}
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
