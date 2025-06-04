package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.commons.UtilFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handle resources.
 */
public abstract class AbstractResourceHandler extends AbstractHandler {
	protected Map<String, Type> mimetypes=new HashMap<>();
	protected Set<String> disallowedFileTypes=new HashSet<>();
	protected boolean allowFolderListing=false;
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
		@Override
		public String toString() {
			return ""+mimeType+(compress?" COMPRESS":" NO COMPRESS");
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
		registerMimeType("opus", "audio/ogg", false);
		registerMimeType("m4a", "audio/mp4", false);
		registerMimeType("ttf", "font/ttf", false);
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
		validatepieces(path, allowFolderListing);
		boolean headQuery=false;
		switch(request.getMethod())
		{
		case "GET":
			break;
		case "HEAD":
			headQuery=true;
			break;
		default:
			throw new IOException("Invalid method: "+request.getMethod());
		}
		if(allowFolderListing && path.isFolder())
		{
			renderFolderListing(path, baseRequest, request, response);
			return;
		}
		String extension=path.getExtension();
		validateExtension(extension);
		String resname=path.toStringPath();
		String mime=getMimeType(extension);
		String acceptEncoding=baseRequest.getHeader("Accept-Encoding");
		String range=baseRequest.getHeader("Range");
		long rangeFrom=0;
		long rangeTo=Long.MAX_VALUE;
		if(range!=null && range.startsWith("bytes="))
		{
			//System.out.println("Range query: "+range);
			String spec=range.substring("bytes=".length());
			int idxComma=spec.indexOf(',');
			if(idxComma>=0)
			{
				throw new IllegalArgumentException("Multiple ranges in single query are not supported");
			}
			int idx=spec.indexOf('-');
			if(idx>0) {
				rangeFrom=Long.parseLong(spec.substring(0, idx));
			}
			String to=spec.substring(idx+1);
			if(to.length()>0) {
				rangeTo=Long.parseLong(to)+1;
			}
		}
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
			if(fileOpener.supportsRange())
			{
				response.setHeader("Accept-Ranges", "bytes");
			}
			if(rangeFrom!=0||rangeTo!=Long.MAX_VALUE)
			{
				long l=fileOpener.length();
				if(rangeTo>l) {
					rangeTo=l;
				}
				// System.out.println("Range query detected: "+rangeFrom+" "+rangeTo);
				response.setHeader("Content-Range", "bytes "+rangeFrom+"-"+((rangeTo==Long.MAX_VALUE)?"":(""+(rangeTo-1)))+"/"+l);
				response.setStatus(206); // Partial reply
			}
			if(fileOpener.supportsLength())
			{
				long l=-1;
				if(rangeTo!=Long.MAX_VALUE)
				{
					l=rangeTo-rangeFrom;
				}else
				{
					l=fileOpener.length()-rangeFrom;
				}
				response.setHeader("Content-Length", ""+l);
			}
			baseRequest.setHandled(true);
			if(extension!=null)
			{
				if(mime!=null)
				{
					response.setContentType(mime);
					if(!headQuery)
					{
						byte[] buffer=new byte[UtilFile.defaultBufferSize.get()];
						try(InputStream is=fileOpener.openRange(rangeFrom, rangeTo))
						{
							long remaining=rangeTo-rangeFrom;
							try(OutputStream os=response.getOutputStream())
							{
								while(remaining>0)
								{
									int n=(int)Math.min(buffer.length, remaining);
									int k=is.read(buffer, 0, n);
									if(k<1)
									{
										remaining=0;
									}else
									{
										os.write(buffer, 0, k);
										remaining-=k;
									}
								}
							}
						}
					}
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
	protected void renderFolderListing(HttpPath path, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		throw new RuntimeException();
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
	public static void validateFileName(String s)
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
				(c=='.' || c=='-' || c=='_' || c==' ' || c=='@')
				)
			{
				// Allowed letters
			}else
			{
				throw new IllegalArgumentException("Illegal letter in path: '"+c+"'");
			}
		}
	}
	public static void validatepieces(HttpPath pieces, boolean allowFolder) {
		if(pieces.getPieces().size()==0 && !allowFolder)
		{
			throw new IllegalArgumentException("folder listing not available");
		}
		for(String s: pieces.getPieces())
		{
			validateFileName(s);
		}
	}

}
