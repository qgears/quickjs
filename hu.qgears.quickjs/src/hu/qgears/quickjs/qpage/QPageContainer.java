package hu.qgears.quickjs.qpage;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilComma;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.quickjs.helpers.IPlatform;
import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.helpers.PromiseImpl;
import hu.qgears.quickjs.helpers.QTimer;

/** QuickJS web page instance.
 */
public class QPageContainer implements Closeable, IQContainer, IUserObjectStorage {
	public static String idAttribute = QPageContainer.class.getSimpleName();
	protected static final Logger log=LoggerFactory.getLogger(QPageContainer.class);
	private String identifier = "id";
	private volatile boolean active = true;
	private Map<String, QComponent> components = new HashMap<>();
	private HtmlTemplate jsTemplate=createJsTemplate();
	public final PromiseImpl<QPageContainer> disposedEvent=new PromiseImpl<>();
	private String scriptsAsSeparateFile=null;
	private List<QComponent> additionaComponentTypes=new ArrayList<QComponent>();
	private Map<String, Object> userObjectStorage=new HashMap<String, Object>();
	private String sessionIdParameterName;
	private String sessionId;
	private List<AutoCloseable> closeables;
	private Object closeablesSyncObject=new Object();
	private QTimer currentTimerTask;	
	private IPlatform platform;
	private IQPageContaierContext context;
	/**
	 * The single current active QPage child.
	 */
	private QPage child;
	public final UtilListenableProperty<Boolean> started=new UtilListenableProperty<Boolean>(false);
	/**
	 * Counter for component unique id creator.
	 */
	private int idCtr=0;
	private Map<String, UtilEvent<JSONObject>> customQueryListeners=new HashMap<>();
	private Map<String, UtilEvent<Msg>> customQueryListeners2=new HashMap<>();
	/**
	 * document.visibilityState visible:true hidden:false
	 */
	public final UtilListenableProperty<Boolean> documentVisibilityState=new UtilListenableProperty<Boolean>(true);
	/**
	 * Event fired when user returns to a previous history state of the current page.
	 * (eg. presses prev button)
	 * See also historyPushState()
	 */
	public final UtilEvent<HistoryPopStateEvent> historyPopState=new UtilEvent<>();
	/**
	 * Size of the HTML Window client. Null before first report was received.
	 * Auto-updated by client.
	 */
	public final UtilListenableProperty<BrowserWindowSize> windowClientSize=new UtilListenableProperty<>();
	
	class Message {
		Msg msg;

		public Message(Msg msg) throws NumberFormatException, IOException {
			super();
			this.msg = msg;
		}
		protected void executeTask() throws IOException
		{
			if(!active)
			{
				jsTemplate.write("page.dispose(\"Server side component is already disposed.\");\n");
				return;
			}
			try(NoExceptionAutoClosable c=child.setThreadCurrentPage())
			{
				JSONObject post = (JSONObject)msg.header;
				if (post.has("history_popstate"))
				{
					historyPopState.eventHappened(new HistoryPopStateEvent(QPageContainer.this, post.getString("pathname"), post.getString("search")));
				}
				else if (post.has("custom")) {
					String type=post.getString("type");
					UtilEvent<JSONObject> ev=getCustomQueryListener(type);
					int n=ev.getNListeners();
					ev.eventHappened(post);
					UtilEvent<Msg> ev2=getCustomQueryListener2(type);
					n+=ev2.getNListeners();
					ev2.eventHappened(msg);
					if(n==0)
					{
						log.error("No listener for custom message: "+post);
					}
				}else
				{
					String cid = post.optString("component", null);
					if(getId().equals(cid))
					{
						handleClientPost(msg, post);
					}else
					{
						QComponent ed = components.get(cid);
						if(ed!=null)
						{
							ed.handleClientPost(msg, post);
						}else
						{
							log.error("Client post target does not exist: "+cid+" "+post);
						}
					}
				}
			}
		}
		public void executeOnThread() throws Exception {
			// Proper ordering of messages is done by messaging subsystem
			executeTask();
		}
	}
	public void handleClientPost(Msg msg, JSONObject post) {
		String type=post.getString("type");
		switch(type)
		{
		case "windowSize":
			BrowserWindowSize size=new BrowserWindowSize(post);
			windowClientSize.setProperty(size);
			break;
		case "started":
			started.setProperty(true);
			break;
		default:
			log.error("Unhandled post type: "+post);
		}
	}
	public void processBrowserMessage(Msg msg)
	{
		try {
			// Proper ordering of messages is done by messaging subsystem
			new Message(msg).executeOnThread();
		} catch (Exception e) {
			log.error("Process browser message", e);
		}
	}
	public QPageContainer(String identifier) {
		this.identifier = identifier;
	}
	public void internalSetPlatform(IPlatform platform) {
		this.platform=platform;
	}
	public void internalStartPlatform()
	{
		getPlatform().reinitDisposeTimer();
		getPlatform().startCommunicationWithJs();
		getCustomQueryListener("visibilitychange").addListener(json->{
			documentVisibilityState.setProperty("visible".equals(json.getString("data")));
		});
	}

	public void writeHeaders(final HtmlTemplate parent) {
		if(scriptsAsSeparateFile!=null)
		{
			new HtmlTemplate(parent) {
				public void generate() {
					for(String fname: getPlatform().getJsOrder())
					{
						write("<script language=\"javascript\" type=\"text/javascript\" src=\"");
						writeObject(scriptsAsSeparateFile);
						write("/");
						writeObject(fname);
						write("\"></script>\n");
					}
					for(QComponent c: getAdditionalComponentTypes())
					{
						for(String scriptRef: c.getScriptReferences())
						{
							write("<script language=\"javascript\" type=\"text/javascript\" src=\"");
							writeObject(scriptsAsSeparateFile);
							write("/");
							writeObject(scriptRef);
							write("\"></script>\n");
						}
					}
				}
			}.generate();
		}else
		{
			generateStaticScripts(parent);
		}
		platform.writeHeaders(parent);
	}
	
	public void writePreloadHeaders(final HtmlTemplate parent) {
		if(scriptsAsSeparateFile!=null)
		{
			new HtmlTemplate(parent) {
				public void generate() {
					for(String fname: getPlatform().getJsOrder())
					{
						write("<link rel=\"preload\" href=\"");
						writeObject(scriptsAsSeparateFile);
						write("/");
						writeObject(fname);
						write("\" as=\"script\" />\n");
					}
					for(QComponent c: getAdditionalComponentTypes())
					{
						for(String scriptRef: c.getScriptReferences())
						{
							write("<link rel=\"preload\" href=\"");
							writeObject(scriptsAsSeparateFile);
							write("/");
							writeObject(scriptRef);
							write("\" as=\"script\" />\n");
						}
					}
				}
			}.generate();
		}
		platform.writePreloadHeaders(parent);
	}

	public void generateInitialization(HtmlTemplate initialHtmlTemplate) {
		new HtmlTemplate(initialHtmlTemplate) {
			public void generate() {
				write("<script language=\"javascript\" type=\"text/javascript\">\n// Script that starts the QuickJS communication loop and connects managed objects DOM and JS \nglobalQPage=new QPageContainer('");
				writeObject(identifier);
				write("', ");
				writeObject(getPlatform().getTIMEOUT_DISPOSE_MS());
				write(");\n");
				if(sessionIdParameterName!=null){
					write("globalQPage.setSessionIdParameterAdditional(\"&");
					writeJSValue(sessionIdParameterName);
					write("=");
					writeJSValue(sessionId);
					write("\");\n");
				}
				write("window.addEventListener(\"load\", function(){\n\tvar page=this;\n\tvar args=staticArgs();\n");
				writeObject(jsTemplate.getWriter().toString());
				write("\tpage.start(");
				writeObject(getPlatform().getMode().ordinal());
				write(");\n}.bind(globalQPage), false);\nwindow.addEventListener(\"beforeunload\", function(){\n\tthis.beforeUnload();\n}.bind(globalQPage), false);\nstaticArgs=function()\n{\n\treturn [");
				UtilComma c=new UtilComma(", ");
				for(Object o: jsTemplate.toWebSocketArguments())
				{
					// TODO handle blob!
					writeObject(c.getSeparator());
					write("\"");
					writeJSValue(o.toString());
					write("\"");
				}
				write("];\n};\n</script>\n");
				jsTemplate=createJsTemplate();
			}
		}.generate();
		initialHtmlTemplate=null;
	}

	public void generateStaticScripts(HtmlTemplate parent) {
		new HtmlTemplate(parent) {
			public void generate() {
				try {
					for(String fname: getPlatform().getJsOrder())
					{
						write("<script language=\"javascript\" type=\"text/javascript\">\n");
						writeObject(getPlatform().loadResource(fname));
						write("\n</script>\n");
					}
				} catch (Exception e) {
					log.error("Include scripts statically", e);
				}
			}
		}.generate();
		for(QComponent c: getAdditionalComponentTypes())
		{
			c.generateHeader(parent);
		}
	}
	public void submitToUI(Runnable r) {
		getPlatform().submitToUI(r);
	}
	public <V> Promise<V> submitToUICallable(Callable<V> c) {
		return getPlatform().submitToUICallable(c);
	}
	/**
	 * In case the page is disposed before timeout then the task will not be executed!
	 * To access the timer inside the runnable: see getCurrentTimerTask()
	 * @param timeoutMillis
	 * @param r
	 */
	public QTimer scheduleToUI(long timeoutMillis, Runnable r) {
		return scheduleToUI(timeoutMillis, 0, r);
	}
	/**
	 * When page is disposed the task is cancelled.
	 * To access the timer inside the runnable: see getCurrentTimerTask()
	 * @param timeoutMillis
	 * @param periodMillis
	 * @param r
	 * @return
	 */
	public QTimer scheduleToUI(long timeoutMillis, long periodMillis, Runnable r) {
		QTimer t=getPlatform().startTimer(r, (int)timeoutMillis, (int)periodMillis);
		t.addCloseable(addCloseable(t));
		return t;
	}

	@Override
	public void close() {
		active = false;
	}

	public void add(QComponent qComponent) {
		QComponent prev=components.put(qComponent.getId(), qComponent);
		if(prev!=null)
		{
			throw new RuntimeException("Key already used: "+qComponent.getId());
		}
	}

	public HtmlTemplate getJsTemplate() {
		return jsTemplate;
	}

	/**
	 * Dispose is called on the session dispose event from the Web Servers thread.
	 */
	public void dispose() {
		active=false;
		platform.deregister();
		Runnable onThread=new Runnable() {
			@Override
			public void run() {
				new HtmlTemplate(jsTemplate)
				{
					public void generate() {
						write("page.dispose(\"Server object disposed.\");");
					}	
				}.generate();
				// Do not send dispose of objects to the client: the objects on the disposed page remain visible.
				HtmlTemplate prev=jsTemplate;
				jsTemplate=new HtmlTemplate(new StringWriter());
				jsTemplate.setupWebSocketArguments(false);
				try {
					for(QComponent c: new ArrayList<>(components.values()))
					{
						c.dispose();
					}
				} finally
				{
					jsTemplate=prev;
				}
				List<AutoCloseable> toClose;
				synchronized (closeablesSyncObject) {
					toClose=closeables;
					closeables=null;
				}
				if(toClose!=null)
				{
					for(AutoCloseable c: toClose)
					{
						closeCloseable(c);
					}
				}
				getPlatform().disposeCommunicationToJS();
				disposedEvent.ready(QPageContainer.this);
			}
		};
		QPage currpage=QPage.getCurrent();
		if(currpage!=null && currpage.getParent()==this)
		{
			onThread.run();
		}else
		{
			submitToUI(onThread);
		}
	}

	/**
	 * HTML5 history API to set up a new URL within this page.
	 * see html5_history.asciidoc
	 * @param userVisibleStateName in most browsers this is unused
	 * @param url
	 */
	public void historyPushState(String userVisibleStateName, String url)
	{
		new HtmlTemplate(jsTemplate)
		{
			public void generate() {
				write("if(page.supports_history_api())\n{\n\thistory.pushState(null, \"");
				writeJSValue(userVisibleStateName);
				write("\", \"");
				writeJSValue(url);
				write("\");\n}\n");
			}	
		}.generate();
	}
	/**
	 * HTML5 history API to set up a new URL within this page.
	 * see html5_history.asciidoc
	 * @param userVisibleStateName in most browsers this is unused
	 * @param url
	 */
	public void historyReplaceState(String userVisibleStateName, String url)
	{
		new HtmlTemplate(jsTemplate)
		{
			public void generate() {
				write("if(page.supports_history_api())\n{\n\thistory.replaceState(null, \"");
				writeJSValue(userVisibleStateName);
				write("\", \"");
				writeJSValue(url);
				write("\");\n}\n");
			}	
		}.generate();
	}

	public String getId() {
		return identifier;
	}

	public void setScriptsAsSeparateFile(String absolutePath) {
		this.scriptsAsSeparateFile = absolutePath;
	}

	public void remove(QComponent qComponent) {
		components.remove(qComponent.id);
	}

	@Override
	public QPageContainer getPageContainer() {
		return this;
	}

	@Override
	public IQContainer getParent() {
		return null;
	}
	@Override
	public void addChild(QComponent child) {
		if(!(child instanceof QPage) || this.child!=null)
		{
			throw new IllegalArgumentException("Only a single QPage child is allowed.");
		}
		else
		{
			this.child=(QPage) child;
		}
	}
	@Override
	public void removeChild(QComponent child)
	{
		if(this.child==child)
		{
			this.child=null;
		}else
		{
			throw new IllegalArgumentException("removeChild: argument not child of QPageContainer");
		}
	}

	public List<QComponent> getAdditionalComponentTypes() {
		return additionaComponentTypes;
	}
	public void addAdditionalType(QComponent type)
	{
		additionaComponentTypes.add(type);
	}
	/**
	 * Create a unique component identifier.
	 * @return
	 */
	public String createComponentId() {
		return "generatedId"+(idCtr++);
	}
	public UtilEvent<JSONObject> getCustomQueryListener(String type) {
		synchronized(customQueryListeners)
		{
			UtilEvent<JSONObject> li=customQueryListeners.get(type);
			if(li==null)
			{
				li=new UtilEvent<>();
				customQueryListeners.put(type, li);
			}
			return li;
		}
	}
	public UtilEvent<Msg> getCustomQueryListener2(String type) {
		synchronized(customQueryListeners2)
		{
			UtilEvent<Msg> li=customQueryListeners2.get(type);
			if(li==null)
			{
				li=new UtilEvent<>();
				customQueryListeners2.put(type, li);
			}
			return li;
		}
	}
	@Override
	public Map<String, Object> getUserObjectStorage() {
		return userObjectStorage;
	}
	/**
	 * In case current task is executed as a timer then the current timer is queryed by this query.
	 * @return
	 */
	public QTimer getCurrentTimerTask() {
		return currentTimerTask;
	}
	/**
	 * In case a parameter based session is used then this will not be null.
	 * Page HTML embedder implementation must call this.
	 * @param sessionIdParameterName
	 */
	public void setSessionIdParameterName(String sessionIdParameterName) {
		this.sessionIdParameterName=sessionIdParameterName;
	}
	/**
	 * Page HTML embedder implementation must call this.
	 * @param sessionId
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	/**
	 * Navigate the current page to an other url.
	 * @param url can be relative or absolute url.
	 */
	public void navigateTo(String url) {
		new HtmlTemplate(jsTemplate)
		{
			public void generate() {
				write("document.location='");
				writeJSValue(url);
				write("';\n");
			}
			
		}.generate();
	}
	@Override
	public NoExceptionAutoClosable addCloseable(AutoCloseable closeable) {
		if(!active)
		{
			closeCloseable(closeable);
			return new NoExceptionAutoClosable() {};
		}else
		{
			synchronized (closeablesSyncObject) {
				if(closeables==null)
				{
					closeables=new ArrayList<>();
				}
				closeables.add(closeable);
			}
			return new NoExceptionAutoClosable() {
				@Override
				public void close() {
					synchronized(closeablesSyncObject)
					{
						if(closeables!=null)
						{
							closeables.remove(closeable);
						}
					}
				}
			};
		}
	}
	private void closeCloseable(AutoCloseable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {
			log.error("Closing attached closeable", e);
		}
	}
	/**
	 * Send a reload query to the client.
	 */
	public void sendReload() {
		new HtmlTemplate(getJsTemplate())
		{
			public void gen()
			{
				write("location.reload();");
			}
		}.gen();
	}
	private HtmlTemplate createJsTemplate() {
		HtmlTemplate ret=new HtmlTemplate(new StringWriter());
		ret.setupWebSocketArguments(true);
		return ret;
	}
	/**
	 * Set the current JS template to redirect output from the current one.
	 * Can only be used from QuickJS package.
	 * @param subJs
	 */
	protected void setJsTemplate(HtmlTemplate subJs) {
		this.jsTemplate=subJs;
	}
	/**
	 * The minimal session timeout required to work on case of short times session objects are used.
	 * In case of short lived sessions setSessionToUpdateLastAccessedTime has to be used to register a listener that
	 * keeps the session object alive while the page is alive.
	 * @return
	 */
	public static int getMinimalSessionTimeoutMs() {
		return (int) (IndexedComm.timeoutPingMillis*3);
	}
	/**
	 * Get the platform specific functions accessor.
	 * @return
	 */
	public IPlatform getPlatform() {
		return platform;
	}
	/**
	 * Submit a timer task to be executed now (ASAP) on the UI thread.
	 * Intended to be called by the platform specific timer implementation.
	 * @param t
	 * @param r
	 */
	public void submitTimerTask(QTimer t, Runnable r) {
		submitToUI(()->{
			try {
				if(!t.isClosed())
				{
					currentTimerTask=t;
					try
					{
						r.run();
					}finally
					{
						currentTimerTask=null;
					}
				}
			} catch (Exception e) {
				log.error("Unhandled exception in timer", e);
			}
		});
	}
	public QPage getPage() {
		return child;
	}
	
	public HtmlTemplate internalGetAndRecreateJsTemplate() {
		HtmlTemplate ret=jsTemplate;
		jsTemplate=createJsTemplate();
		return ret;
	}
	public void setPageContext(IQPageContaierContext context) {
		this.context=context;
	}
	public IQPageContaierContext getPageContext() {
		return context;
	}
}
