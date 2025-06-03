package hu.qgears.quickjs.utils;

import java.io.IOException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;

import hu.qgears.commons.NoExceptionAutoClosable;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * What is it really good for?
 */
@Deprecated
public class PathContextHandler extends HandlerCollection {
	AbstractHandler dh;
	public PathContextHandler(AbstractHandler dh) {
		addHandler(dh);
		this.dh=dh;
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String ctx=UtilHttpContext.getContext(baseRequest);
		String newTarget=null;
		if(ctx!=null && ctx.length()>0)
		{
			if(target.startsWith(ctx))
			{
				newTarget=target.substring(ctx.length());
			}
		}
		if(newTarget!=null)
		{
			try(NoExceptionAutoClosable c=DispatchHandler.setContext(baseRequest, ctx, newTarget))
			{
				dh.handle(newTarget, baseRequest, request, response);
			}
		}else
		{
			dh.handle(target, baseRequest, request, response);
		}
	}
}
