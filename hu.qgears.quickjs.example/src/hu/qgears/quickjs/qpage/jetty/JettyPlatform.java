package hu.qgears.quickjs.qpage.jetty;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.SafeTimerTask;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilTimer;
import hu.qgears.quickjs.helpers.IPlatformServerSide;
import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.helpers.PromiseImpl;
import hu.qgears.quickjs.helpers.QTimer;
import hu.qgears.quickjs.qpage.EQPageMode;
import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.qpage.ISessionUpdateLastAccessedTime;
import hu.qgears.quickjs.qpage.IndexedComm;
import hu.qgears.quickjs.qpage.QPageContainer;
import hu.qgears.quickjs.qpage.QPageManager;
import hu.qgears.quickjs.serverside.QPageTypesRegistry;

public class JettyPlatform implements IPlatformServerSide {
	private static final Logger log=LoggerFactory.getLogger(JettyPlatform.class);
	private final QPageManager qpm;
	QPageContainer pageContainer;
	private volatile Thread thread;
	private SafeTimerTask disposeTimer;
	private EQPageMode mode=EQPageMode.serverside;
	private Object syncObject = new Object();
	private long TIMEOUT_DISPOSE=IndexedComm.timeoutPingMillis*2;
	private Executor executor;
	private LinkedList<Runnable> tasks=new LinkedList<>();
	private ISessionUpdateLastAccessedTime sessionToUpdateLastAccessedTime;
	private IndexedComm indexedComm;
	private String path;
	private Supplier<NoExceptionAutoClosable> setupContext=()->(new NoExceptionAutoClosable() {});
	private Map<String, IndexedComm> customWebSocketImplementations=Collections.synchronizedMap(new HashMap<>());

	public JettyPlatform(QPageContainer pageContainer, QPageManager qpm) {
		this.qpm=qpm;
		this.pageContainer=pageContainer;
		thread=Thread.currentThread();
	}
	private class JettyQTimer extends QTimer
	{
		SafeTimerTask stt;
		@Override
		public void close() {
			stt.cancel();
			super.close();
		}
	}
	@Override
	public QTimer startTimer(Runnable r, int firstTimeoutMs, int periodMs) {
		JettyQTimer t=new JettyQTimer();
		t.stt=new SafeTimerTask() {
			@Override
			protected void doRun() {
				pageContainer.submitTimerTask(t, r);
				pageContainer.submitToUI(()->{
					try {
						if(!isCancelled())
						{
							r.run();
						}
					} catch (Exception e) {
						log.error("Unhandled exception in timer", e);
					}
				});
			}
		};
		UtilTimer.javaTimer.schedule(t.stt, firstTimeoutMs, periodMs);
		return t;
	}
	public void setExecutor(Executor executor) {
		thread=null;
		this.executor = executor;
		scheduleNext();
	}
	@Override
	public boolean isQPageThread()
	{
		return thread==Thread.currentThread();
	}
	/**
	 * Internal API.
	 * Can be used in the very rare case when for some reason the page is blocked for a time.
	 * Calling this periodically will disable page disposal by timer.
	 */
	@Override
	public void reinitDisposeTimer() {
		switch(mode)
		{
		case hybrid:
			/// In hybrid mode the server side objects are disposed at once after initial reply and they are not handled by a timer.
			return;
		case serverside:
			break;
		default:
			throw new RuntimeException("Mode not handled: "+mode);
		}
		if(disposeTimer!=null)
		{
			disposeTimer.cancel();
			disposeTimer=null;
		}
		disposeTimer=new SafeTimerTask() {
			@Override
			public void doRun() {
				pageContainer.dispose();
			}
		};
		QPageManager.disposeTimer.schedule(disposeTimer, TIMEOUT_DISPOSE);
	}
	@Override
	public int getTIMEOUT_DISPOSE_MS() {
		return (int)TIMEOUT_DISPOSE;
	}
	@Override
	public void startCommunicationWithJs() {
		switch(mode)
		{
		case serverside:
			break;
		case hybrid:
			return;
		default:
			throw new RuntimeException("Unhandled mode: "+mode);
		}
		qpm.register(pageContainer.getId(), pageContainer);
		indexedComm=new IndexedComm();
		indexedComm.received.addListener(msg->{
			JSONObject jo=(JSONObject)msg.header;
			if(jo.has("log"))
			{
				JSONArray a=jo.getJSONArray("log");
				for(Object o: a)
				{
					log.info(""+o);
				}
			}else if(jo.has("unload")&&"true".equals(jo.get("unload"))) {
				handleUnloadQuery();
			}else if(jo.has("history_popstate")|| jo.has("custom")||jo.has("component"))
			{
				synchronized (syncObject) {
					// Proper ordering of messages is done by messaging subsystem
					thread=Thread.currentThread();
					try
					{
						synchronized (syncObject) {
							try(NoExceptionAutoClosable c=pageContainer.getPage().setThreadCurrentPage())
							{
								try(NoExceptionAutoClosable s=setupContext.get())
								{
									pageContainer.processBrowserMessage(msg);
								}
							}finally
							{
								HtmlTemplate jsTemplate=pageContainer.internalGetAndRecreateJsTemplate();
								indexedComm.sendMessage("js", jsTemplate.toWebSocketArguments());
								reinitDisposeTimer();
							}
						}
					}finally
					{
						thread=null;
					}
				}
			}
			if(sessionToUpdateLastAccessedTime!=null)
			{
				try {
					sessionToUpdateLastAccessedTime.setLastAccessedTime(System.currentTimeMillis());
				} catch (Exception e) {
					log.error("sessionToUpdateLastAccessedTime", e);
				}
			}
		});
		indexedComm.receivedPing.addListener(msg->{
			reinitDisposeTimer();
		});
	}
	public void handleUnloadQuery() {
		// Unload means that the client does not accept more messages: comm has to be closed immediately.
		indexedComm.close();
		pageContainer.dispose();
	}
	/**
	 * Set the session object callback to extend the timeout of the session.
	 * @param sessionToUpdateLastAccessedTime
	 * @return
	 */
	public QPageContainer setSessionToUpdateLastAccessedTime(ISessionUpdateLastAccessedTime sessionToUpdateLastAccessedTime)
	{
		this.sessionToUpdateLastAccessedTime=sessionToUpdateLastAccessedTime;
		return pageContainer;
	}
	/**
	 * Change dispose timeout for this page.
	 * This timeout may be very long then the QPage object is stored until the application becomes online again.
	 * @param timeoutMillis
	 * @return this object itself
	 */
	public QPageContainer setDisposeTimeout(long timeoutMillis)
	{
		this.TIMEOUT_DISPOSE=timeoutMillis;
		reinitDisposeTimer();
		return pageContainer;
	}
	protected void scheduleNext() {
		synchronized(tasks)
		{
			if(tasks.size()>0)
			{
				if(executor!=null)
				{
					executor.execute(new Runnable() {
						@Override
						public void run() {
							Runnable r=tasks.peek();
							while(r!=null)
							{
								synchronized (syncObject) {
									try (NoExceptionAutoClosable c=pageContainer.getPage().setThreadCurrentPage()) {
										thread=Thread.currentThread();
										try(AutoCloseable s=setupContext.get())
										{
											r.run();
										}
									} catch (Exception e) {
										log.error("send JS to client", e);
									} finally
									{
										HtmlTemplate jsTemplate=pageContainer.internalGetAndRecreateJsTemplate();
										indexedComm.sendMessage("js", jsTemplate.toWebSocketArguments());
										thread=null;
									}
								}
								synchronized (tasks) {
									tasks.remove();
									if(tasks.size()>0)
									{
										r=tasks.peek();
									}else
									{
										r=null;
									}
								}
							}
						}
					});
				}
			}
		}
	}
	@Override
	public IndexedComm getIndexedComm() {
		return indexedComm;
	}
	/**
	 * When messages are processed on the WebSocket thread then
	 * this supplier can be used to set up and tear down the context
	 * for processing.
	 * Can also be used to do synchronization between pages.
	 * @param setupContext
	 */
	public void setSetupContext(Supplier<NoExceptionAutoClosable> setupContext) {
		this.setupContext = setupContext;
	}
	@Override
	public void disposeCommunicationToJS() {
		if(indexedComm!=null)
		{
			/**
			 * Timeout is required so that the last messages are sent first hopefully.
			 */
			UtilTimer.javaTimer.schedule(new SafeTimerTask() {
				@Override
				public void doRun() {
					indexedComm.close();
				}
			}, 2000);
		}
	}
	@Override
	public void submitToUI(Runnable r) {
		if(!pageContainer.disposedEvent.isDone())
		{
			synchronized (tasks) {
				tasks.add(r);
				// Otherwise we have one task already being executed
				if(tasks.size()==1)
				{
					scheduleNext();
				}
			}
		}
	}
	@Override
	public <V> Promise<V> submitToUICallable(Callable<V> c) {
		PromiseImpl<V> ret=new PromiseImpl<V>();
		if(!pageContainer.disposedEvent.isDone())
		{
			synchronized (tasks) {
				tasks.add(()->{
					try
					{
						V v=c.call();
						ret.ready(v);
					}catch(Exception e)
					{
						ret.error(e);
					}
					catch(Throwable t)
					{
						ret.error(t);
						throw t;
					}
				});
				// Otherwise we have one task already being executed
				if(tasks.size()==1)
				{
					scheduleNext();
				}
			}
		}else
		{
			ret.error(new IllegalStateException("Page already disposed"));
		}
		return ret;
	}
	
	@Override
	public IndexedComm getCustomWebsocketImplementation(String string) {
		return customWebSocketImplementations.get(string);
	}
	@Override
	public String registerCustomWebsocketImplementation(IndexedComm websocketImplementation) {
		String id=pageContainer.createComponentId();
		customWebSocketImplementations.put(id, websocketImplementation);
		return id;
	}
	@Override
	public void unregisterCustomWebsocketImplementation(String id) {
		customWebSocketImplementations.remove(id);
	}
	@Override
	public void deregister() {
		qpm.remove(pageContainer);
	}
	@Override
	public QPageManager getQPageManager() {
		return qpm;
	}
	public void setMode(EQPageMode mode) {
		this.mode=mode;
	}
	@Override
	public EQPageMode getMode() {
		return mode;
	}
	private String teaVMJs;
	public void setTeaVMJs(String teaVMJs) {
		this.teaVMJs=teaVMJs;
	}
	@Override
	public void writePreloadHeaders(HtmlTemplate parent) {
		if(teaVMJs!=null)
		{
			new HtmlTemplate(parent)
			{
				void mw()
				{
					write("<link rel=\"preload\" href=\"");
					writeObject(pageContainer.getPageContext().getResourcePathSync(teaVMJs));
					write("\" as=\"script\" />\n");
				}
			}.mw();
		}
	}
	@Override
	public void writeHeaders(HtmlTemplate parent) {
		if(teaVMJs!=null)
		{
			new HtmlTemplate(parent)
			{
				void mw()
				{
					write("<script language=\"javascript\" type=\"text/javascript\" src=\"");
					writeObject(pageContainer.getPageContext().getResourcePathSync(teaVMJs));
					write("\"></script>\n");
				}
			}.mw();
		}
	}
	@Override
	public List<String> getJsOrder() {
		return QPageTypesRegistry.getInstance().getJsOrder();
	}
	@Override
	public String loadResource(String fname) throws IOException {
		URL url=QPageTypesRegistry.getInstance().getResource(fname);
		if(url==null)
		{
			throw new IOException("Does not exist: "+fname);
		}
		return UtilFile.loadAsString(url);
	}
	public void setPath(String path) {
		this.path=path;
	}
	@Override
	public void configureJsGlobalQPage(HtmlTemplate parent, String string) {
		new HtmlTemplate(parent)
		{
			void ws()
			{
				writeObject(string);
				write(".setPath(\"");
				writeJSValue(path);
				write("\");\n");
			}
		}.ws();
	}
}
