package hu.qgears.quickjs.upload;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.server.Request;

import hu.qgears.quickjs.qpage.HtmlTemplate;
import jakarta.servlet.http.HttpServletResponse;

public class UploadHandler extends HtmlTemplate
{
	private File uploadFolder;
	private UploadHandlerDelegate delegate=new UploadHandlerDelegate();
	public UploadHandler(File uploadFolder) {
		this.uploadFolder=uploadFolder;
	}

	public void handle(Request baseRequest, HttpServletResponse response) {
		if(delegate.handle(uploadFolder, baseRequest, response, true))
		{
			return;
		}
		response.setContentType("text/html; charset=utf-8");
		try {
			setWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
			write("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\">\n<head>\n<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n\n<title>");
			writeHtml(getTitle());
			write("</title>\n\n");
			headPreScript();
			write("<script type=\"text/javascript\" src=\"./upload.js\"></script>\n<script type=\"text/javascript\">\nprogressBytes=0;\nfunction handleFiles(files)\n{\n\tconsole.info(\"Upload files selected: \"+files);\n\tvar numFiles = files.length;\n\tfor (var i = 0, numFiles = files.length; i < numFiles; i++) {\n\t\tvar file = files[i];\n\t\tconsole.log(\"File to handle: \"+file.name+\" \"+file.size);\n\t\tfu=new FileUpload(file, ");
			writeObject(delegate.getMaxChunkSize());
			write(");\n\t\tfu.progress=function(file, bytes){showProgress(file, bytes, \"\"); };\n\t\tfu.error=function(file, bytes){\n\t\t\tshowProgress(file, bytes, \" ERROR\");\n\t\t\tvar restart=document.getElementById(\"restart\");\n\t\t\trestart.style.display=\"inline\";\n\t\t};\n\t\tfu.finished=function(file, bytes){showProgress(file, bytes, \" FINISHED\"); };\n\t\tfu.start();\n\t}\n}\nfunction restart()\n{\n\tfu.start();\n\tvar restart=document.getElementById(\"restart\");\n\trestart.style.display=\"none\";\n}\nfunction showProgress(file, bytes, msg)\n{\n\tprogressBytes=Math.max(bytes,progressBytes);\n\tdocument.getElementById(\"progress\").innerHTML=file.name+\" - \"+progressBytes+\" bytes\"+msg;\n}\n</script>\n</head>\n<h1>");
			writeHtml(getTitle());
			write("</h1>\n\n");
			generateHTMLBeforeUploadPart();
			write("\n<input type=\"file\" id=\"input\" onchange=\"handleFiles(this.files)\">\n\n<div id=\"progress\"></div>\n<button id=\"restart\" style=\"display:none;\" onclick=\"restart()\">Restart upload</button>\n\n");
			generateHTMLAfterUploadPart();
			write("\n</body>\n</html>\n");
			getWriter().flush();
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		baseRequest.setHandled(true);
	}
	/**
	 * Subclass may emit HTML content into the page before the upload script.
	 */
	protected void headPreScript() {
	}

	/**
	 * Subclasses may extend HTML page content.
	 */
	protected void generateHTMLAfterUploadPart() {}
	/**
	 * Subclasses may extend HTML page content.
	 */
	protected void generateHTMLBeforeUploadPart() {}

	protected String getTitle() {
		return "Example Big file upload";
	}

	public void setMaxChunkSize(int i) {
		delegate.setMaxChunkSize(i);
	}
}
