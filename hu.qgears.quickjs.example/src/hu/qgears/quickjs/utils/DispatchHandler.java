package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import hu.qgears.commons.UtilString;

public class DispatchHandler extends AbstractHandler{
	private Map<String, AbstractHandler> handlers=new HashMap<>();
	private FolderContext root=new FolderContext();
	class FolderContext
	{
		Map<String, FolderContext> contexts=new HashMap<>();
		List<AbstractHandler> handlers=new ArrayList<>();
		AbstractHandler[] handlersArray=null;
		public void register(List<String> pieces, AbstractHandler handler) {
			if(pieces.size()==0)
			{
				handlers.add(handler);
				handlersArray=null;
			}else
			{
				FolderContext c=contexts.get(pieces.get(0));
				if(c==null)
				{
					c=new FolderContext();
					contexts.put(pieces.get(0), c);
				}
				c.register(pieces.subList(1, pieces.size()), handler); 
			}
		}
		public AbstractHandler[] getHandlersArray() {
			AbstractHandler[] ret;
			synchronized (DispatchHandler.this) {
				if(handlersArray==null)
				{
					handlersArray=handlers.toArray(new AbstractHandler[]{});
				}
				ret=handlersArray;
			}
			return ret;
		}
	}
	synchronized public void addHandler(String context, AbstractHandler handler)
	{
		handlers.put(context, handler);
		List<String> pieces=UtilString.split(context, "/");
		root.register(pieces, handler);
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		List<String> pieces=UtilString.split(target, "/");
		boolean folder=target.endsWith("/");
		int nFolder=pieces.size();
		if(!folder)
		{
			nFolder--;
		}
		FolderContext found=root;
		FolderContext current=root;
		int nDepth=0;
		synchronized (this) {
			for(int i=0;i<nFolder;++i)
			{
				FolderContext sub=current.contexts.get(pieces.get(i));
				if(sub==null)
				{
					break;
				}else
				{
					current=sub;
					nDepth=i+1;
					found=current;
				}
			}
		}
		if(found!=null)
		{
			StringBuilder path=new StringBuilder();
			path.append("/");
			for(int i=nDepth;i<pieces.size();++i)
			{
				path.append(pieces.get(i));
				if(i<pieces.size()-1 || folder)
				{
					path.append("/");
				}
			}
			String newTarget=path.toString();
			baseRequest.setPathInfo(newTarget);
			try
			{
				for(AbstractHandler h: found.getHandlersArray())
				{
					h.handle(newTarget, baseRequest, request, response);
					if(baseRequest.isHandled())
					{
						break;
					}
				}
			}finally
			{
				baseRequest.setPathInfo(target);
			}
		}
	}
}
