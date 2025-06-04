package quickjs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.server.Request;

import hu.qgears.quickjs.qpage.HtmlTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class IndexHtml extends HtmlTemplate
{
	public void serve(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		Writer wr=new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
		setWriter(wr);
		generateResponse();
		wr.close();
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	}

	private void generateResponse() throws IOException {
		write("<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n\n<title>Example minimal web server</title>\n\n\n<script type=\"text/javascript\">\ninputhandler=function(event)\n{\n\tvar xhr = new XMLHttpRequest();\n\txhr.responseType = \"text\";\n\txhr.onreadystatechange = function() {\n\t\tif (this.readyState == 4 && this.status == 200) {\n\t\t\tdocument.getElementById(\"responseDiv\").innerHTML=this.responseText;\n\t\t}\n\t}.bind(xhr);\n\tvar url=\"query?id=id\";\n\tvar inputs = document.getElementsByTagName('input');\n\tfor (var index = 0; index < inputs.length; ++index) {\n\t\tvar i=inputs[index];\n\t\turl=url+\"&\"+i.id+\"=\"+encodeURIComponent(i.value);\n\t}\n\txhr.open(\"POST\",url);\n\txhr.send();\n}\nwindow.onload = function(e){ \n\tvar inputs, index;\n\t\n\tinputs = document.getElementsByTagName('input');\n\tfor (index = 0; index < inputs.length; ++index) {\n\t\tvar i=inputs[index];\n\t\ti.onchange = inputhandler;\n\t\ti.onkeypress=inputhandler;\n\t\ti.onpaste=inputhandler;\n\t\ti.oninput=inputhandler;\n\t}\n}\n</script>\n\n</head>\n\n\n<h1>Example minimal Web server</h1>\n<a href=\"/\">Back to index</a><br/>\n\n<table>\n");
		for(int i=0;i<11;++i){
			write("<tr>\n<td>Line ");
			writeObject(i);
			write(" row 1: <input type=\"text\" id=\"input-");
			writeObject(i);
			write("-1\" name=\"input-");
			writeObject(i);
			write("-1\"></td>\n<td>Line ");
			writeObject(i);
			write(" row 2: <input id=\"input-");
			writeObject(i);
			write("-2\"></td>\n</tr>\n");
		}
		write("</table>\n<div id=\"responseDiv\"></div>\n\n</html>\n");
	}
}
