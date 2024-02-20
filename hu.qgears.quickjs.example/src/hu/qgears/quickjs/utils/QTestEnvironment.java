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
import hu.qgears.quickjs.qpage.QPage;

/**
 * Test environment for testing web UI.
 * QuickJS pages are instantiated asynchronously to the test code
 * and this class is used to access QPage instances when they are instantiated
 * to the test code.
 */
public class QTestEnvironment implements IQTestEnvironment {
	Map<Function<Request, Boolean>, SignalFutureWrapper<QPage>> awaits=Collections.synchronizedMap(new HashMap<Function<Request,Boolean>, SignalFutureWrapper<QPage>>());
	@Override
	public void qPageCreated(Request baseRequest, QPage newPage) {
		List<Function<Request, Boolean>> found=new ArrayList<Function<Request,Boolean>>();
		List<SignalFutureWrapper<QPage>> signals=new ArrayList<SignalFutureWrapper<QPage>>();
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
		for(SignalFutureWrapper<QPage> s: signals)
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
	public SignalFutureWrapper<QPage> createNewPageListener(Function<Request, Boolean> filter)
	{
		SignalFutureWrapper<QPage> ret=new SignalFutureWrapper<QPage>();
		awaits.put(filter, ret);
		SignalFutureWrapper<QPage> ret2=new SignalFutureWrapper<QPage>();
		ret.addOnReadyHandler(e->{
			QPage p=e.getSimple();
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
	public SignalFutureWrapper<QPage> createNewPageListener()
	{
		return createNewPageListener(r->true);
	}
	private SignalFutureWrapper<QPage> newPage;
	/**
	 * Start listening to a new page and store the reference to the new page listener.
	 * New page can be queried using the getNewPage() method
	 */
	public void startListenNewPage()
	{
		newPage=createNewPageListener();
	}
	public QPage getNewPage() throws InterruptedException, ExecutionException, TimeoutException
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
