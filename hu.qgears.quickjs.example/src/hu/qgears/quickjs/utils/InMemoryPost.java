package hu.qgears.quickjs.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.MultiMap;

import hu.qgears.quickjs.qpage.IInMemoryPost;

public class InMemoryPost implements IMultipartHandler, IInMemoryPost
{
	private Map<String, ByteArrayOutputStream> parameters=new HashMap<>();
	public InMemoryPost(Request baseRequest) throws Exception {
		new InMemoryMultiPartInputStreamParser(baseRequest.getInputStream(), baseRequest.getContentType(), this).parse();
	}

	@Override
	public OutputStream createPart(String name, String filename, MultiMap<String> headers, String contentType)
			throws IOException {
		ByteArrayOutputStream ret=new ByteArrayOutputStream();
		parameters.put(name, ret);
		return ret;
	}
	public String getParameter(String name)
	{
		ByteArrayOutputStream bos=parameters.get(name);
		if(bos==null)
		{
			return null;
		}
		return new String(bos.toByteArray(), StandardCharsets.UTF_8);
	}
	public Map<String, ByteArrayOutputStream> getParameters() {
		return parameters;
	}
}
