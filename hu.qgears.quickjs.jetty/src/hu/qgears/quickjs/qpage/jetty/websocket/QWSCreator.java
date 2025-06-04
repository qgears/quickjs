package hu.qgears.quickjs.qpage.jetty.websocket;

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
	public Object createWebSocket(WebSocketCreationContext c) {
		Object wsc=c.r.getAttribute(WebSocketCreator.class.getSimpleName());
		if(wsc instanceof WebSocketCreator && wsc!=this)
		{
			WebSocketCreator creator=(WebSocketCreator) wsc;
			return creator.createWebSocket(c);
		}
		final QPageManager qpm=HttpSessionQPageManager.getManager(c.r.getSession());
		String id=c.r.getParameter(QPageContainer.class.getSimpleName());
		final QPageContainer page=qpm.getPage(id);
		if(page==null)
		{
			log.info("Page Websocket Query finds no page: pageid: "+id+" session id: '"+c.r.getSession().getId()+"'");
			return null;
		}else
		{
			IPlatformServerSide pss=(IPlatformServerSide)page.getPlatform();
			String customId=c.r.getParameter("customId");
			if(customId!=null)
			{
				IndexedComm ic=pss.getCustomWebsocketImplementation(customId);
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
