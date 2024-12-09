package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.MultiMapHashImpl;
import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilString;
import hu.qgears.quickjs.qpage.example.QPageContext;

public class DispatchHandler extends HandlerCollection {
	private Logger log=LoggerFactory.getLogger(getClass());
	private AbstractHandler trapHandler=null;
	private AbstractHandler errorHandler=null;
	private QPageContext contextToSetup;
	private FolderContext root=new FolderContext("root");
	private static AbstractHandler[] empty=new AbstractHandler[] {};
	public class FolderContext
	{
		private String id;
		
		public FolderContext(String id) {
			super();
			this.id = id;
		}
		Map<String, FolderContext> contexts=new HashMap<>();
		MultiMapHashImpl<String, AbstractHandler> handlers=new MultiMapHashImpl<String, AbstractHandler>();
		/**
		 * Cached quick find and iterate read-only map.
		 */
		Map<String, AbstractHandler[]> handlersArray=new HashMap<String, AbstractHandler[]>();
		public void register(List<String> pieces, String resource, AbstractHandler handler) {
			if(resource!=null && !resource.startsWith("/"))
			{
				throw new RuntimeException("Resource name must start with /");
			}
			if(pieces.size()==0)
			{
				handlers.putSingle(resource, handler);
				handlersArray.clear();
			}else
			{
				FolderContext c=contexts.get(pieces.get(0));
				if(c==null)
				{
					c=new FolderContext(pieces.get(0));
					contexts.put(pieces.get(0), c);
				}
				c.register(pieces.subList(1, pieces.size()), resource, handler); 
			}
		}
		public AbstractHandler[] getHandlersArray(String resource) {
			AbstractHandler[] ret;
			synchronized (DispatchHandler.this) {
				ret=handlersArray.get(resource);
				if(ret==null)
				{
					List<AbstractHandler> l=handlers.getPossibleNull(resource);
					if(l==null)
					{
						return empty;
					}
					ret=l.toArray(new AbstractHandler[]{});
					handlersArray.put(resource, ret);
				}
			}
			return ret;
		}
		@Override
		public String toString() {
			return "Folder Context: "+id;
		}
	}
	synchronized public void addHandler(String context, AbstractHandler handler)
	{
		List<String> pieces=UtilString.split(context, "/");
		root.register(pieces, null, handler);
		// Pass lifecycle events to children
		addHandler(handler);
	}
	synchronized public void addHandler(String context, String resource, AbstractHandler handler)
	{
		List<String> pieces=UtilString.split(context, "/");
		root.register(pieces, resource, handler);
		// Pass lifecycle events to children
		addHandler(handler);
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if(baseRequest.getAttribute("originalTarget")==null)
		{
			logRequest(target, baseRequest, request, response);
			baseRequest.setAttribute("originalTarget", target);
		}
		// Initialize the session object if does not exist yet. Required for websocket
		// entry point to work in case the first query is already a websocket query
		// Such as in case of our https port proxy solution.
		request.getSession();
		QPageContext prevContext=null;
		if(contextToSetup!=null)
		{
			prevContext=QPageContext.getCurrent();
			QPageContext.setCurrent(contextToSetup);
		}
		preHandle(target, baseRequest, request, response);
		try
		{
			try
			{
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
					StringBuilder contextPath=new StringBuilder();
					String context=UtilHttpContext.getContext(baseRequest);
					contextPath.append(context);
					contextPath.append("/");
					for(int i=0;i<nDepth;++i)
					{
						contextPath.append(pieces.get(i));
						contextPath.append("/");
					}
					try(NoExceptionAutoClosable reset=setContext(baseRequest, contextPath.toString(), newTarget))
					{
						// Exact match within context
						for(AbstractHandler h: found.getHandlersArray(newTarget))
						{
							h.handle(newTarget, baseRequest, request, response);
							if(baseRequest.isHandled())
							{
								break;
							}
						}
						if(!baseRequest.isHandled())
						{
							// Default handler within context
							for(AbstractHandler h: found.getHandlersArray(null))
							{
								h.handle(newTarget, baseRequest, request, response);
								if(baseRequest.isHandled())
								{
									break;
								}
							}
						}
					}
				}
			}catch(Exception e)
			{
				// We do not show internal exceptions to the user but send the generic trap or 404 if trap is not set up.
				log.error("Handling HTTP query "+target ,e);
				try
				{
					baseRequest.setAttribute("exception", e);
					if(errorHandler!=null)
					{
						errorHandler.handle(target, baseRequest, request, response);
					}
				}catch(Exception ex)
				{
					log.error("Error handler has thrown exception" ,ex);
				}
			}
			try
			{
				if(trapHandler!=null && !baseRequest.isHandled())
				{
					trapHandler.handle(target, baseRequest, request, response);
				}
			}catch(Exception ex)
			{
				log.error("Trap handler has thrown exception" ,ex);
			}
		}finally
		{
			postHandle(target, baseRequest, request, response);
			if(contextToSetup!=null)
			{
				QPageContext.setCurrent(prevContext);
			}
		}
	}
	/**
	 * Override to disable stdout logging of all queries
	 * @param target
	 * @param baseRequest
	 * @param request
	 * @param response
	 */
	protected void logRequest(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) {
		String upgrade=baseRequest.getHeader("Upgrade");
		// Logging the parameter map in case of POST invalidates the input stream object!
		System.out.println("At: "+System.currentTimeMillis()+" "+new Date()+" Target: "+target+
				(upgrade==null?"":(" UPGRADE:"+upgrade))
				// +" "+("POST".equals(baseRequest.getMethod())? "" :baseRequest.getParameterMap())+" "+System.currentTimeMillis()
				);
	}
	protected void preHandle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) {
	}
	protected void postHandle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) {
	}
	public static NoExceptionAutoClosable setContext(Request baseRequest, String contextPath, String newTarget)
	{
		String prePathInfo=baseRequest.getPathInfo();
		String preContextPath=baseRequest.getContextPath();
		// Context path is the path that can be used to access this handler's owner folder
		baseRequest.setContextPath(contextPath);
		baseRequest.setPathInfo(newTarget);
		return new NoExceptionAutoClosable() {
			@Override
			public void close() {
				baseRequest.setPathInfo(prePathInfo);
				baseRequest.setContextPath(preContextPath);
			}
		};
	}
	/**
	 * This handler is called in case all other handlers did not handle this request.
	 * The error handler does not handle the errors of this handler because it is executed after the error handler.
	 * @param trapHandler
	 */
	public void setTrapHandler(AbstractHandler trapHandler) {
		this.trapHandler = trapHandler;
	}
	/**
	 * This handler is called in case the primary handler has thrown an exception.
	 * the exception is accessible on the attribute "exception" of baseRequest
	 * @param errorHandler
	 */
	public void setErrorHandler(AbstractHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
	public void setContextToSetup(QPageContext contextToSetup) {
		this.contextToSetup = contextToSetup;
	}
}
