package hu.qgears.quickjs.teavm;

import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;
import org.teavm.jso.JSBody;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.qpage.AbstractQPage;
import hu.qgears.quickjs.qpage.EQPageMode;
import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.qpage.Msg;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.qpage.QPageContainer;
import hu.qgears.quickjs.serialization.ClientSideCallContextData;
import hu.qgears.quickjs.serialization.RemoteMessageObject;
import hu.qgears.quickjs.serialization.RemotingAllBase;
import hu.qgears.quickjs.serialization.SerializeBase;
import hu.qgears.quickjs.spa.QSpa;

public class TeaVMGui implements JSQPageCallback {
	private QPageContainer qpc;
	private AbstractQPage currpage;
	private TeaVMQPageContainer tea;
	private LinkedList<Msg> messages=new LinkedList<>();
	private SerializeBase serialize;
	private RemotingAllBase remoting;
	TeaVMRemotingImplementation remotingImpl;
	private Object session;
	private ClientSideCallContextData data;
	QSpa spa;
	public TeaVMGui() {
	}
	public void init()
	{
		this.remotingImpl=new TeaVMRemotingImplementation();
		remotingImpl.setSerializator(serialize);
		remoting.initialize(remotingImpl);
		TeaVMQPageContainer pageContainer=setTeaVMCallback(this);
		this.tea=pageContainer;
		remotingImpl.setPageContainer(pageContainer);
	}
	public ClientSideCallContextData getClientSideCallContextData()
	{
		if(data==null)
		{
			byte[] initializContextData=tea.getContextObjectSerialized();
			serialize.setInput(ByteBuffer.wrap(initializContextData));
			data=(ClientSideCallContextData)serialize.deserializeObject();
		}
		return data;
	}
	
	@JSBody(params = { "callback" }, script = "return globalQPage.setTeaVMCallback(callback)")
	public static native TeaVMQPageContainer setTeaVMCallback(JSQPageCallback callback);

	@Override
	public void openPath(String path) {
		
		TeaVMQuery q=new TeaVMQuery();
		spa.query(q, path, 0);
		if(QPage.getCurrent()!=null)
		{
			// TODO this path is not tried or tested
			QPage.getCurrent().dispose();
		}
		QPage page=new QPage(qpc);
		// In Javascript TeaVM the single page is always current until a SOA navigation opens a new QPage
		// Has to be set before the page is initialized
		page.setThreadCurrentPage();
		try {
			currpage=q.found.fact.createPage();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		TeaVMQPageContainerContext context=new TeaVMQPageContainerContext(page, data, session);
		context.initializeObject=data.initObject;
		context.remoting=remoting;
		
		List<RemoteMessageObject> replay=new ArrayList<>();
		int nReplayObject=tea.getNReplayObject();
		for(int i=0;i<nReplayObject;++i)
		{
			byte[] ro=tea.getReplayObject(i);
			serialize.setInput(ByteBuffer.wrap(ro));
			RemoteMessageObject roo=(RemoteMessageObject)serialize.deserializeObject();
			roo.setAsBinary(ro);
			// System.out.println("Replay object: "+roo);
			replay.add(roo);
		}
		remotingImpl.setReplayObjects(replay);
		
		currpage.setPageContext(context);
		qpc.setPageContext(context);
		HtmlTemplate initialHmtl=new HtmlTemplate(new StringWriter());
		try(NoExceptionAutoClosable c=currpage.setInitialHtmlOutput(initialHmtl))
		{
			currpage.initPage();
			currpage.initialCreateHtml();
		}
		remotingImpl.signalReplayEnded();
		// System.out.println("Inital html generated and dropped because of server side rendering");
		tea.requestProcessMessages();
	}

	@Override
	public void createPageContainer(String identifier, int mode) {
		// System.out.println("Create page container "+identifier);
		qpc=new QPageContainer(identifier);
		TeaVMPlatform platform=new TeaVMPlatform(this, EQPageMode.values()[mode]);
		platform.setSerializator(serialize);
		qpc.internalSetPlatform(platform);
		qpc.internalStartPlatform();
	}
	@Override
	public void msgHeader(String header) {
		Msg m=new Msg();
		m.header=new JSONObject(header);
		m.arguments=new Object[] {};
		messages.add(m);
		tea.requestProcessMessages();
	}
	@Override
	public void processMessages() {
		while(!messages.isEmpty())
		{
			Msg m=messages.removeFirst();
			qpc.processBrowserMessage(m);
		}
		evalCollectedJs();
	}
	public void evalCollectedJs() {
		HtmlTemplate js=qpc.internalGetAndRecreateJsTemplate();
		Object[] args=js.toWebSocketArguments();
		tea.javaMessageBegin();
		// System.out.println("JS: "+js.getWriter().toString()+" args.length: "+args.length);
		//tea.javaMessageArgString(js.getWriter().toString());
		for(Object o :args)
		{
			// System.out.println("JS ARG: "+o);
			if(o instanceof String)
			{
				tea.javaMessageArgString((String) o);
			}else
			{
				tea.javaMessageArgBytes((byte[]) o);
			}
		}
		tea.javaMessage("js");
	}
	public void setSerializator(SerializeBase serialize) {
		this.serialize=serialize;
	}
	public void setRemoting(RemotingAllBase remoting) {
		this.remoting=remoting;
	}
	@Override
	public void channelOpened() {
		remotingImpl.channelOpened();
		evalCollectedJs();
	}
	@Override
	public void channelReady() {
		remotingImpl.channelReady();
		evalCollectedJs();
	}
	@Override
	public void messageReceived(byte[] data) {
		remotingImpl.messageReceived(data);
		evalCollectedJs();
	}
	@Override
	public void channelError() {
		remotingImpl.channelError();
		evalCollectedJs();
	}
	@Override
	public void channelClosed() {
		remotingImpl.channelClosed();
		evalCollectedJs();
	}
	public void setRemotingBase(Object session) {
		this.session=session;
	}
	public void setSpa(QSpa spa) {
		this.spa=spa;
	}
}
