package hu.qgears.quickjs.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.eclipse.jetty.server.Request;

import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.quickjs.qpage.QPageContainer;

/**
 * Test environment for testing web UI.
 * QuickJS pages are instantiated asynchronously to the test code
 * and this class is used to access QPage instances when they are instantiated
 * to the test code.
 */
public class QTestEnvironment implements IQTestEnvironment {
	Map<Function<Request, Boolean>, SignalFutureWrapper<QPageContainer>> awaits=Collections.synchronizedMap(new HashMap<Function<Request,Boolean>, SignalFutureWrapper<QPageContainer>>());
	@Override
	public void qPageCreated(Request baseRequest, QPageContainer newPage) {
		List<Function<Request, Boolean>> found=new ArrayList<Function<Request,Boolean>>();
		List<SignalFutureWrapper<QPageContainer>> signals=new ArrayList<SignalFutureWrapper<QPageContainer>>();
		synchronized (awaits) {
			for(Function<Request, Boolean> filter: awaits.keySet())
			{
				if(filter.apply(baseRequest))
				{
					found.add(filter);
				}
			}
			for(Function<Request, Boolean> filter: found)
			{
				signals.add(awaits.remove(filter));
			}
		}
		for(SignalFutureWrapper<QPageContainer> s: signals)
		{
			s.ready(newPage, null);
		}
		IQTestEnvironment.super.qPageCreated(baseRequest, newPage);
	}
	/**
	 * Create a Future object that becomes valid once the required page is served to the client.
	 * @param filter
	 * @return
	 */
	public SignalFutureWrapper<QPageContainer> createNewPageListener(Function<Request, Boolean> filter)
	{
		SignalFutureWrapper<QPageContainer> ret=new SignalFutureWrapper<QPageContainer>();
		awaits.put(filter, ret);
		SignalFutureWrapper<QPageContainer> ret2=new SignalFutureWrapper<QPageContainer>();
		ret.addOnReadyHandler(e->{
			QPageContainer p=e.getSimple();
			if(p!=null)
			{
				p.started.addListenerWithInitialTrigger(ev->{
					if(ev)
					{
						ret2.ready(p, null);
					}
				});
			}else
			{
				ret2.ready(null, e.getThrowable());
			}
		});
		return ret2;
	}
	public SignalFutureWrapper<QPageContainer> createNewPageListener()
	{
		return createNewPageListener(r->true);
	}
	private SignalFutureWrapper<QPageContainer> newPage;
	/**
	 * Start listening to a new page and store the reference to the new page listener.
	 * New page can be queried using the getNewPage() method
	 */
	public void startListenNewPage()
	{
		newPage=createNewPageListener();
	}
	public QPageContainer getNewPage() throws InterruptedException, ExecutionException, TimeoutException
	{
		try
		{
			return newPage.get(10, TimeUnit.SECONDS);
		}finally
		{
			newPage=null;
		}
	}
}
