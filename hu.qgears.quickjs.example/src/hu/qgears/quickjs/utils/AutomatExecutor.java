package hu.qgears.quickjs.utils;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.qpage.QPage;

/**
 * Execute an automaton script in a page.
 */
@Deprecated
abstract public class AutomatExecutor extends HtmlTemplate implements UtilEventListener<JSONObject>
{
	protected QPage page;
	private StringWriter jsWriter=new StringWriter();
	private long queryId;
	private Map<Long, SignalFutureWrapper<Object>> queries=new HashMap<>();
	private long msQueryTimeout=10000;
	private Logger log=LoggerFactory.getLogger(getClass());
	public AutomatExecutor(QPage page) {
		super();
		this.page = page;
	}
	/**
	 * Start a new thread and execute executeScript() on that thread.
	 */
	public void start()
	{
		new Thread("automat executor")
		{
			public void run() {
				try {
					executeScript();
				} catch (Exception e) {
					log.error("Executing automate script",e);
				}
			};
		}
		.start();
	}
	/**
	 * Execute a script on the page.
	 * Must be executed on a separate thread.
	 * @throws Exception
	 */
	public void executeScript() throws Exception
	{
		page.getCustomQueryListener("automat").addListener(this);
		try
		{
			setWriter(jsWriter);
			doExecute();
			executeCommands();
		}finally
		{
			page.getCustomQueryListener("automat").removeListener(this);
		}
	}
	protected abstract void doExecute() throws Exception;
	protected void executeCommands() {
		String s=jsWriter.toString();
		if(s.length()>0)
		{
			jsWriter=new StringWriter();
			setWriter(jsWriter);
			page.submitToUI(()->{
				new HtmlTemplate(page.getJsTemplate())
				{
					public void a() {
						writeObject(s);
					}
				}.a();
			});
		}
	}
	protected Object executeQuery(Runnable r) throws InterruptedException, ExecutionException, TimeoutException
	{
		StringWriter q=new StringWriter();
		Writer prev=getWriter();
		setWriter(q);
		try
		{
			r.run();
		}finally
		{
			setWriter(prev);
		}
		long id=queryId++;
		write("page.sendJson(\"automat\", {\"id\": ");
		writeObject(id);
		write(", \"data\":");
		writeObject(q.toString());
		write("});\n");
		SignalFutureWrapper<Object> ret=new SignalFutureWrapper<>();
		synchronized (queries) {
			queries.put(id, ret);
		}
		executeCommands();
		return ret.get(msQueryTimeout, TimeUnit.MILLISECONDS);
	}
	@Override
	public void eventHappened(JSONObject msg) {
		JSONObject root=msg.getJSONObject("json");
		long id=root.getLong("id");
		synchronized (queries) {
			SignalFutureWrapper<Object> q=queries.remove(id);
			if(q!=null)
			{
				q.ready(root.get("data"), null);
			}
		}
	}
}
