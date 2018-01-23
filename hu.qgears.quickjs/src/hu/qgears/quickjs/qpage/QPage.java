package hu.qgears.quickjs.qpage;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.signal.SignalFutureWrapper;

/**
 * @author rizsi
 *
 */
public class QPage implements Closeable {
	public static String idAttribute = QPage.class.getSimpleName();
	private String identifier = "id";
	private volatile boolean active = true;
	private Map<String, QComponent> components = new HashMap<>();
	private Object syncObject = new Object();
	private int currentMessageIndex = 0;
	private int serverstateindex = 0;
	private HtmlTemplate currentTemplate;
	public boolean inited;
	private final QPageManager qpm;
	private static long TIMEOUT_POLL=15000;
	private static long TIMEOUT_DISPOSE=TIMEOUT_POLL*2;
	private LinkedBlockingQueue<Runnable> tasks=new LinkedBlockingQueue<>();
	public final SignalFutureWrapper<QPage> disposedEvent=new SignalFutureWrapper<>();
	private volatile Thread thread;
	private String scriptsAsSeparateFile=null;
	private List<QComponent> toInit=new ArrayList<>();
	
	class MessageFramingTemplate extends HtmlTemplate {

		public MessageFramingTemplate(HtmlTemplate parent) {
			super(parent);
		}

		public void openMessage() {
			write("page.processServerMessage(");
			writeObject(serverstateindex);
			write(",function(page)\n{\n\tpage.resetDisposeTimeout();\n");
			serverstateindex++;
		}

		public void closeMessage() {
			write("});\n");
		}

	}

	class Message {
		HtmlTemplate parent;
		IInMemoryPost post;
		int index;
		boolean outOfOrder=false;

		public Message(HtmlTemplate parent, IInMemoryPost post) throws NumberFormatException, IOException {
			super();
			this.parent = parent;
			this.post = post;
			index = Integer.parseInt(post.getParameter("messageindex"));
		}
		public Message(HtmlTemplate parent, boolean outOfOrder) throws NumberFormatException, IOException {
			super();
			this.parent = parent;
			this.outOfOrder=outOfOrder;
		}
		protected void executeTask() throws IOException
		{
			String cid = post.getParameter("component");
			QComponent ed = components.get(cid);
			ed.handle(parent, post);
		}

		public void executeOnThread() throws Exception {
			long t = System.currentTimeMillis();
			synchronized (syncObject) {
				// Proper ordering of messages!
				if(!outOfOrder)
				{
					while (index != currentMessageIndex) {
						syncObject.wait(10000);
						if (System.currentTimeMillis() > t + 10000) {
							// TODO crash the client! User feedback of internal
							// error!
							throw new TimeoutException();
						}
					}
					currentMessageIndex++;
				}
				MessageFramingTemplate msft = new MessageFramingTemplate(parent);
				thread=Thread.currentThread();
				currentTemplate=parent;
				msft.openMessage();
				executeTask();
				for(QComponent c: toInit)
				{
					c.init();
				}
				toInit.clear();
				currentTemplate=null;
				msft.closeMessage();
				syncObject.notifyAll();
				reinitDisposeTimer();
				thread=null;
			}
		}
	}
	private TimerTask disposeTimer;
	public QPage(QPageManager qpm) {
		this.qpm=qpm;
		identifier = qpm.createId();
		qpm.register(identifier, this);
		reinitDisposeTimer();
	}

	private void reinitDisposeTimer() {
		if(disposeTimer!=null)
		{
			disposeTimer.cancel();
			disposeTimer=null;
		}
		disposeTimer=new TimerTask() {
			
			@Override
			public void run() {
				dispose();
			}
		};
		QPageManager.disposeTimer.schedule(disposeTimer, TIMEOUT_DISPOSE);
	}

	public void writeHeaders(final HtmlTemplate parent) {
		if(scriptsAsSeparateFile!=null)
		{
			new HtmlTemplate(parent) {
				public void generate() {
					write("<script language=\"javascript\" type=\"text/javascript\" src=\"");
					writeObject(scriptsAsSeparateFile);
					write("/QPage.js\"></script>\n");
					for(QComponent c: QPageTypesRegistry.getInstance().getTypes())
					{
						for(String scriptRef: c.getScriptReferences())
						{
							write("<script language=\"javascript\" type=\"text/javascript\" src=\"");
							writeObject(scriptsAsSeparateFile);
							write("/");
							writeObject(scriptRef);
							write(".js\"></script>\n");
						}
					}
				}
			}.generate();
		}else
		{
			generateStaticScripts(parent);
		}
	}

	public void generateInitialization(HtmlTemplate parent) {
		currentTemplate=parent;
		new HtmlTemplate(parent) {
			public void generate() {
				write("<script language=\"javascript\" type=\"text/javascript\">\nglobalQPage=new QPage('");
				writeObject(identifier);
				write("', ");
				writeObject(TIMEOUT_DISPOSE);
				write(");\nwindow.addEventListener(\"load\", function(){\n\tvar page=this;\n");
					currentTemplate=this;
					for (QComponent c : components.values()) {
						c.init();
					}
					currentTemplate=null;
					write("\tpage.start();\n}.bind(globalQPage), false);\nwindow.addEventListener(\"beforeunload\", function(){\n\tthis.beforeUnload();\n}.bind(globalQPage), false);\n</script>\n");
			}
		}.generate();
		currentTemplate=null;
		inited=true;
	}

	public void generateStaticScripts(HtmlTemplate parent) {
		new HtmlTemplate(parent) {
			public void generate() {
				try {
					write("<script language=\"javascript\" type=\"text/javascript\">\n");
					writeObject(UtilFile.loadAsString(QPage.class.getResource("QPage.js")));
					write("\n</script>\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.generate();
		for(QComponent c: QPageTypesRegistry.getInstance().getTypes())
		{
			c.generateHeader(parent);
		}
	}

	public boolean handle(HtmlTemplate parent, IInMemoryPost post) throws IOException {
		if(!active)
		{
			new HtmlTemplate(parent)
			{
				public void generate() {
					write("page.dispose(\"Server side compontent is already disposed.\");\n");
				}
			}.generate();
		}else
		{
			if ("true".equals(post.getParameter("periodic"))) {
				handlePeriodicQuery(parent);
				return true;
			}
			if ("true".equals(post.getParameter("unload"))) {
				handleUnloadQuery(parent);
				return true;
			}
			Message m = new Message(parent, post);
			try {
				m.executeOnThread();
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		return true;
	}
	private void handleUnloadQuery(HtmlTemplate parent) {
		dispose();
	}

	public void submitToUI(Runnable r) {
		if(!disposedEvent.isDone())
		{
			tasks.add(r);
		}
	}

	private void handlePeriodicQuery(HtmlTemplate parent) {
		try {
			final Runnable task=tasks.poll(TIMEOUT_POLL, TimeUnit.MILLISECONDS);
			new Message(parent, true)
			{
				protected void executeTask() throws IOException {
					try{
						if(task!=null)
						{
							task.run();
						}
						while(!tasks.isEmpty())
						{
							Runnable t=tasks.poll();
							t.run();
						}
					}catch(Exception e)
					{
						// TODO
						e.printStackTrace();
					}
					if (active) {
						parent.write("page.query();\n");
					}
				};
			}.executeOnThread();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		new HtmlTemplate(parent) {
//			public void generate() {
//				write("console.info(\"Hello QPage reply! \"+page);\n");
//				if (active) {
//					write("page.query();\n");
//				}
//			}
//		}.generate();
	}

	@Override
	public void close() {
		active = false;
	}

	public void add(QComponent qTextEditor) {
		components.put(qTextEditor.getId(), qTextEditor);
	}

	public HtmlTemplate getCurrentTemplate() {
		return currentTemplate;
	}

	/**
	 * Dispose is called on the session dispose event from the Web Servers thread.
	 */
	public void dispose() {
		active=false;
		disposedEvent.ready(this, null);
		qpm.remove(this);
		if(currentTemplate!=null)
		{
			generateDisposeJSCall();
		}else
		{
			submitToUI(new Runnable() {
				@Override
				public void run() {
				}
			});
		}
	}

	private void generateDisposeJSCall() {
		new HtmlTemplate(currentTemplate)
		{
			public void generate() {
				write("page.dispose(\"Server object disposed.\")");
			}	
		}.generate();
	}

	public boolean isThread() {
		return Thread.currentThread()==thread;
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

	public void registerToInit(QComponent qComponent) {
		toInit.add(qComponent);
	}

	public void setCurrentTemplate(HtmlTemplate parent) {
		currentTemplate=parent;
	}
}
