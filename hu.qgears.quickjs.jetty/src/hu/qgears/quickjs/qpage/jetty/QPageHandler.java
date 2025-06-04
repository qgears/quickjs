package hu.qgears.quickjs.qpage.jetty;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.qpage.AbstractQPage;
import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.qpage.IQPageFactory;
import hu.qgears.quickjs.qpage.ISessionUpdateLastAccessedTime;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.qpage.QPageContainer;
import hu.qgears.quickjs.qpage.QPageManager;
import hu.qgears.quickjs.qpage.example.QPageContext;
import hu.qgears.quickjs.qpage.jetty.websocket.QApiMessagingClass;
import hu.qgears.quickjs.qpage.jetty.websocket.QWSMessagingServlet;
import hu.qgears.quickjs.serialization.RemotingServer;
import hu.qgears.quickjs.serialization.SerializeBase;
import hu.qgears.quickjs.serverside.QPageContextServerSide;
import hu.qgears.quickjs.spa.QSpa;
import hu.qgears.quickjs.spa.RoutingEndpointQPage;
import hu.qgears.quickjs.utils.HttpSessionQPageManager;
import hu.qgears.quickjs.utils.IQTestEnvironment;
import hu.qgears.quickjs.utils.UtilHttpContext;
import hu.qgears.quickjs.utils.UtilJetty;
import hu.qgears.quickjs.utils.gdpr.GdprSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Jetty compatible http query handler that includes an {@link AbstractQPage}.
 * 
 * This is the only Jetty specific part of the example implementation.
 * This logic must be reimplemented to include QPage applications within a servlet for example.
 */
public class QPageHandler extends HandlerCollection {
	private static Logger log=LoggerFactory.getLogger(QPageHandler.class);
	private QSpa pageFactory;
	private Function<QueryWrapperJetty, Object> userParameterGetter;
	private IContextConfigurator contextConfigurator=new IContextConfigurator() {
		@Override
		public void configurePageContext(QPage page, QPageContextServerSide context, QueryWrapperJetty queryWrapper) {
			// Default implementation does nothing
		}
		@Override
		public SerializeBase createSerializator() {
			throw new RuntimeException("IContextConfigurator not configured");
		}
		public RemotingServer createRemotingServer(hu.qgears.quickjs.qpage.IQDisposableContainer container, GdprSession session) {
			throw new RuntimeException("IContextConfigurator not configured");
		};
	};
	public static final String key=QPageHandler.class.getName()+".objectParameter";
	private Map<String, AbstractHandler> webSocketHandlers=new HashMap<>();
	public static void setUserParameter(ServletRequest request, Object userParameter)
	{
		request.setAttribute(key, userParameter);
	}
	public QPageHandler(QSpa pageFactory) {
		this.pageFactory=pageFactory;
		createWebSocketEntry();
	}
	public QPageHandler(Class<? extends AbstractQPage> pageClass) {
		this.pageFactory=new QSpa();
		pageFactory.method("GET").handle(new RoutingEndpointQPage(()->pageClass.getDeclaredConstructor().newInstance()));
			// req->{AbstractQPage ret=pageClass.getConstructor().newInstance(); 
			// ret.setUserData(req); return ret;};
		createWebSocketEntry();
	}
	public QPageHandler(IQPageFactory pageCreator) {
		this.pageFactory=new QSpa();
		pageFactory.method("GET").handle(new RoutingEndpointQPage(
				pageCreator));
			// req->{AbstractQPage ret=pageClass.getConstructor().newInstance(); 
			// ret.setUserData(req); return ret;};
		createWebSocketEntry();
	}
	private void createWebSocketEntry() {
		AbstractHandler websocketHandler=QWSMessagingServlet.createHandler();
		addWebSocketHandler("true", websocketHandler);
		AbstractHandler apiHandler=QApiMessagingClass.createHandler(this);
		addWebSocketHandler("api", apiHandler);
		try {
			pageFactory.configureWebsocketHandlers(this);
		} catch (Exception e) {
			log.error("createWebocketEntry", e);
		}
	}
	public void addWebSocketHandler(String string, AbstractHandler websocketHandler) {
		addHandler(websocketHandler);
		webSocketHandlers.put(string, websocketHandler);
	}
	@Override
	public void handle(String target, final Request baseRequest, jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws IOException {
		HttpSession sess=baseRequest.getSession();
		final QPageManager qpm=HttpSessionQPageManager.getManager(sess);
		String id=baseRequest.getParameter(QPageContainer.class.getSimpleName());
		if(!isStarted())
		{
			throw new RuntimeException("Handler is used but not started");
		}
		String websocket=baseRequest.getParameter("websocket");
		AbstractHandler selectedWebsocketHandler=webSocketHandlers.get(websocket);
		
		if(selectedWebsocketHandler!=null)
		{
			try {
				// Fix issue with nginx passthrough:
				// Ngninx unencodes the path which causes exception in ServletupgradeRequest
				// For some reason all non-websocket queries work fine.
				// We do not use the path information in websocket factory so we just
				// remove the path as a workaround.
				/**
				 * Caused by: java.net.URISyntaxException: Illegal character in path at index 36: ws://localhost:9092/queryresult/VIN="qwerty"?websocket=true&QPage=1602001882489_4
 at java.base/java.net.URI$Parser.fail(URI.java:2913)
at java.base/java.net.URI$Parser.checkChars(URI.java:3084)
at java.base/java.net.URI$Parser.parseHierarchical(URI.java:3166)
at java.base/java.net.URI$Parser.parse(URI.java:3114)
 at java.base/java.net.URI.<init>(URI.java:600)
 at org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest.<init>(ServletUpgradeRequest.java:65)
				 */
				// baseRequest.setURIPathQuery("/");
				selectedWebsocketHandler.handle(target, baseRequest, request, response);
			} catch (Exception e) {
				log.error("Handle websocket query", e);
			}
			return;
		}
		try {
			// QPage requests are always genuine and they have no use to cache.
			UtilJetty.setResponseNotCacheable(response);
			response.setContentType("text/html; charset=utf-8");
			final Writer wr=new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
			switch(baseRequest.getMethod())
			{
			case "GET":
				if(id==null)
				{
					// handle initial get
					new HtmlTemplate(wr){
						public void generate() throws Exception {
							String id=qpm.createId();
							QPageContainer newPage=new QPageContainer(id);
							QPage page=new QPage(newPage);
							QPageContextServerSide context=new QPageContextServerSide(page, UtilHttpContext.getContext(baseRequest));
							newPage.setPageContext(context);
							QueryWrapperJetty queryWrapper=new QueryWrapperJetty(target, baseRequest, request, response);
							JettyPlatform platform=new JettyPlatform(newPage, qpm);
							platform.setQueryWrapper(queryWrapper);
							platform.setPath(target);
							newPage.internalSetPlatform(platform);
							Object userParameter=userParameterGetter==null?null:(userParameterGetter.apply(queryWrapper));
							if(userParameter!=null)
							{
								platform.setUserParameter(userParameter);
							}
							contextConfigurator.configurePageContext(page, context, queryWrapper);
							newPage.internalStartPlatform();
							QPageContext qpc=QPageContext.getCurrent();
							{
								Object attrib=sess.getAttribute(GdprSession.keyUseNoCookieSession);
								if(attrib instanceof Boolean)
								{
									boolean useNoCookieSession=(Boolean)attrib;
									if(useNoCookieSession)
									{
										newPage.setSessionIdParameterName(GdprSession.keySessionIdParameterName);
									}
								}
							}
							newPage.setSessionId(baseRequest.getSession().getId());
							HttpSession session=request.getSession();
							if(session instanceof ISessionUpdateLastAccessedTime)
							{
								platform.setSessionToUpdateLastAccessedTime((ISessionUpdateLastAccessedTime) session);
							}
							page.addCloseable(()->{
								context.closeEvent.eventHappened(context);
							});
							try(NoExceptionAutoClosable c=QPage.setThreadCurrentPage(page))
							{
								boolean handled=pageFactory.query(queryWrapper, target, 0);
								if(!handled)
								{
									throw new RuntimeException("Routing did not find QPage for path: "+target);
								}
								if(queryWrapper.routingEndpoint instanceof RoutingEndpointQPage)
								{
									AbstractQPage inst=((RoutingEndpointQPage)queryWrapper.routingEndpoint).fact.createPage();
									inst.setPageContext(context);
									inst.initPage();
									try(NoExceptionAutoClosable close=inst.setInitialHtmlOutput(this))
									{
										inst.initialCreateHtml();
									}
								}
							}
							context.pageInitializedEvent.eventHappened(context);
							switch(platform.getMode())
							{
							case hybrid:
							{
								/// In hybrid mode execution is continued on the client side in TeaVM in browser. This instance done its job.
								try(NoExceptionAutoClosable c= QPage.setThreadCurrentPage(page))
								{
									newPage.dispose();
								}
								break;
							}
							case serverside:
							{
								platform.setExecutor(r->{
									try
									{
										Server server=getServer();
										if(server!=null)
										{
											ThreadPool tp=server.getThreadPool();
											if(!newPage.disposedEvent.isDone() && server.isRunning())
											{
												tp.execute(r);
											}
										}
									}catch(Exception e)
									{
										log.error("executor error", e);
									}
								});
								break;
							}
							default:
								throw new RuntimeException("Unknown mode: "+platform.getMode());
							}
							if(qpc!=null)
							{
								IQTestEnvironment testEnvironment=qpc.getTestEnvironment();
								if(testEnvironment!=null)
								{
									testEnvironment.qPageCreated(baseRequest, newPage);
								}
							}
						}
					}.generate();
				}else
				{
					log.error("Page exists with id: "+id+" query invalid!");
				}
				break;
			default:
				break;
			}
			baseRequest.setHandled(true);
			wr.close();
		}catch(Exception e)
		{
			if(e instanceof EOFException)
			{
				// Client closed page before content was sent to it - normal no need to log exception.
				log.info("Page query closed by client: "+target+" "+baseRequest.getMethod());
			}
			throw new IOException("Processing page: "+target+" "+baseRequest.getMethod()+" "+baseRequest.getParameter("QPage"), e);
		}
	}
	public QPageHandler setContextConfigurator(IContextConfigurator contextConfigurator) {
		this.contextConfigurator = contextConfigurator;
		return this;
	}
	public IContextConfigurator getContextConfigurator() {
		return contextConfigurator;
	}
	public QPageHandler setUserParameterGetter(Function<QueryWrapperJetty, Object> userParameterGetter) {
		this.userParameterGetter=userParameterGetter;
		return this;
	}
}
