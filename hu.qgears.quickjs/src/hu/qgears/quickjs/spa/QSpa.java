package hu.qgears.quickjs.spa;

import hu.qgears.quickjs.qpage.AbstractQPage;
import hu.qgears.quickjs.qpage.IQPageContaierContext;
import hu.qgears.quickjs.qpage.IQPageFactory2;
import hu.qgears.quickjs.qpage.QQueryWrapper;

/** Single page application domain. Collects all pages that are accessible without reload.
 * In case of a traditional webapp (no SPA) each AbstractQPage has to be wrapped into one QSpa */
public class QSpa {
	IQPageFactory2 supplier;
	/**
	 * Create a page for the query.
	 * @param query
	 * @param context 
	 * @return
	 * @throws Exception 
	 */
	public AbstractQPage createPage(QQueryWrapper query, IQPageContaierContext context) throws Exception {
		return supplier.createPage().setPageContext(context);
	}
	/**
	 * Configure additional Websocket connections on the server that are accessible
	 * from the client.
	 * TODO specify and implement this feature.
	 * @param qPageHandler2
	 */
	public void configureWebsocketHandlers(Object qPageHandler2) {
		// TODO Auto-generated method stub
		
	}
	public QSpa on(String path, IQPageFactory2 supplier) {
		this.supplier=supplier;
		// TODO Feature only stubbed instead of real implementation
		return this;
	}
}
