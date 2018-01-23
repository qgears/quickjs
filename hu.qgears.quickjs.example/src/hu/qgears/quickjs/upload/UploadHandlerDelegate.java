package hu.qgears.quickjs.upload;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import hu.qgears.commons.UtilFile;
import hu.qgears.quickjs.utils.InMemoryMultiPartInputStreamParser;

/**
 * Handles queries to upload.js and POST queries that push the file content from the browser to the server.
 */
public class UploadHandlerDelegate
{
	public UploadHandlerDelegate() {
	}
	private long maxChunkSize=5000000;

	public boolean handle(File uploadFolder, Request baseRequest, HttpServletResponse response, boolean handleJs) {
		String pathinfo=baseRequest.getPathInfo();
		if(handleJs&&"/upload.js".equals(pathinfo))
		{
			response.setContentType("application/x-javascript");
			try(OutputStream os=response.getOutputStream())
			{
				os.write(UtilFile.loadFile(UploadHandlerDelegate.class.getResource("upload.js")));
				response.setStatus(HttpServletResponse.SC_OK);
			}catch(Exception e)
			{
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				e.printStackTrace();
			}
			baseRequest.setHandled(true);
			return true;
		}else if("POST".equals(baseRequest.getMethod()) && "/upload".equals(pathinfo))
		{
			try {
				UploadFileHandler h=new UploadFileHandler(uploadFolder);
				InMemoryMultiPartInputStreamParser p=new InMemoryMultiPartInputStreamParser(baseRequest.getInputStream(), baseRequest.getContentType(), h);
				p.setMaxRequestSize(maxChunkSize+1000000).parse();
				response.setContentType("text/html");
				h.writeResponse(response.getOutputStream());
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (Exception e1) {
				try {
					response.setContentType("text/html");
					response.getOutputStream().write("ERROR".getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					e.printStackTrace();
				}
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				e1.printStackTrace();
			}
			baseRequest.setHandled(true);
			return true;
		}
		return false;
	}

	public long getMaxChunkSize() {
		return maxChunkSize;
	}
}
