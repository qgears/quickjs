package hu.qgears.quickjs.qpage.example.websocket;

import java.util.List;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.quickjs.helpers.IPlatformServerSide;
import hu.qgears.quickjs.qpage.IndexedComm;
import hu.qgears.quickjs.qpage.QPageContainer;
import hu.qgears.quickjs.qpage.QPageManager;
import hu.qgears.quickjs.utils.HttpSessionQPageManager;

public class QWSCreator implements WebSocketCreator {

	Logger log=LoggerFactory.getLogger(getClass());
	@Override
	public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse arg1) {
		Object wsc=req.getServletAttribute(WebSocketCreator.class.getSimpleName());
		if(wsc instanceof WebSocketCreator && wsc!=this)
		{
			WebSocketCreator creator=(WebSocketCreator) wsc;
			return creator.createWebSocket(req, arg1);
		}
		final QPageManager qpm=HttpSessionQPageManager.getManager(req.getSession());
		String id=req.getHttpServletRequest().getParameter(QPageContainer.class.getSimpleName());
		final QPageContainer page=qpm.getPage(id);
		if(page==null)
		{
			log.info("Page Websocket Query finds no page: pageid: "+id+" session id: '"+req.getSession().getId()+"'");
			return null;
		}else
		{
			IPlatformServerSide pss=(IPlatformServerSide)page.getPlatform();
			List<String> customId=req.getParameterMap().get("customId");
			if(customId!=null && customId.size()==1)
			{
				IndexedComm ic=pss.getCustomWebsocketImplementation(customId.get(0));
				if(ic!=null)
				{
					QWSMessagingClass ret=new QWSMessagingClass();
					ret.setIndexedComm(ic);
					return ret;
				}
				return null;
			}
			QWSMessagingClass ret=new QWSMessagingClass();
			ret.setIndexedComm(pss.getIndexedComm());
			return ret;
		}
	}

}
