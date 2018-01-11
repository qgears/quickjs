package hu.qgears.quickjs.upload;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import hu.qgears.commons.UtilFile;
import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.utils.InMemoryMultiPartInputStreamParser;

public class UploadHandler extends HtmlTemplate
{
	private File uploadFolder;
	public UploadHandler(File uploadFolder) {
		this.uploadFolder=uploadFolder;
	}
	private long maxChunkSize=5000000;

	public void handle(Request baseRequest, HttpServletResponse response) {
		String pathinfo=baseRequest.getPathInfo();
		if("/upload.js".equals(pathinfo))
		{
			response.setContentType("application/x-javascript");
			try(OutputStream os=response.getOutputStream())
			{
				os.write(UtilFile.loadFile(getClass().getResource("upload.js")));
				response.setStatus(HttpServletResponse.SC_OK);
			}catch(Exception e)
			{
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				e.printStackTrace();
			}
			baseRequest.setHandled(true);
		}else if("/upload".equals(pathinfo))
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
		}
		else
		{
			response.setContentType("text/html; charset=utf-8");
			try {
				setWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
				write("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\">\n<head>\n<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n\n<title>");
				writeHtml(getTitle());
				write("</title>\n\n<script type=\"text/javascript\" src=\"./upload.js\"></script>\n<script type=\"text/javascript\">\nfunction handleFiles(files)\n{\n\tvar numFiles = files.length;\n\tfor (var i = 0, numFiles = files.length; i < numFiles; i++) {\n\t\tvar file = files[i];\n\t\tconsole.log(\"File to handle: \"+file.name+\" \"+file.size);\n\t\tfu=new FileUpload(file, ");
				writeObject(maxChunkSize);
				write(");\n\t\tfu.progress=function(file, bytes){showProgress(file, bytes, \"\"); };\n\t\tfu.error=function(file, bytes){\n\t\t\tshowProgress(file, bytes, \" ERROR\");\n\t\t\tvar restart=document.getElementById(\"restart\");\n\t\t\trestart.style.display=\"inline\";\n\t\t};\n\t\tfu.finished=function(file, bytes){showProgress(file, bytes, \" FINISHED\"); };\n\t\tfu.start();\n\t}\n}\nfunction restart()\n{\n\tfu.start();\n\tvar restart=document.getElementById(\"restart\");\n\trestart.style.display=\"none\";\n}\nfunction showProgress(file, bytes, msg)\n{\n\tdocument.getElementById(\"progress\").innerHTML=file.name+\" - \"+bytes+\" bytes\"+msg;\n}\n</script>\n</head>\n\n<h1>");
				writeHtml(getTitle());
				write("</h1>\n\n<input type=\"file\" id=\"input\" onchange=\"handleFiles(this.files)\">\n\n<div id=\"progress\"></div>\n<button id=\"restart\" style=\"display:none;\" onclick=\"restart()\">Restart upload</button>\n</body>\n</html>\n");
				getWriter().flush();
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (IOException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				e.printStackTrace();
			}
			baseRequest.setHandled(true);
		}
	}

	private String getTitle() {
		return "Example Big file upload";
	}

}
