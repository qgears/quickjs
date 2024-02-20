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
public class ResourceClassPathHandler extends AbstractHandler {
	Class<?> clazz;
	private Map<String, String> mimetypes=new HashMap<>();
	public ResourceClassPathHandler(Class<?> clazz) {
		this.clazz=clazz;
		mimetypes.put("js", "text/javascript");
		mimetypes.put("css", "text/css");
		mimetypes.put("svg", "image/svg+xml");
		mimetypes.put("jpg", "image/jpg");
		mimetypes.put("png", "image/png");
		mimetypes.put("html", "text/html");
		mimetypes.put("webp", "image/webp");
		mimetypes.put("doc", "application/msword");
		mimetypes.put("docx", "application/vnd.openxmlformats");
		mimetypes.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
		mimetypes.put("fods", "application/vnd.oasis.opendocument.spreadsheet-flat-xml");
		mimetypes.put("pdf", "application/pdf");
		mimetypes.put("md", "text/plain");
		mimetypes.put("txt", "text/plain");
		mimetypes.put("json", "text/json");
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		handle(new HttpPath(target), baseRequest, request, response);
	}
	private void handle(HttpPath path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		validatepieces(path);
		String extension=path.getExtension();
		String resname=path.toStringPath();
		URL url=clazz.getResource(resname);
		if(url!=null)
		{
			baseRequest.setHandled(true);
			if(extension!=null)
			{
				String mime=getMimeType(extension);
				if(mime!=null)
				{
					response.setContentType(mime);
					byte[] data=UtilFile.loadFile(url);
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
	public String getMimeType(String extension) {
		return mimetypes.get(extension);
	}
	public String getMimeTypeSafe(String extension) {
		String ret=mimetypes.get(extension);
		if(ret==null)
		{
			ret="application/binary";
		}
		return ret;
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
