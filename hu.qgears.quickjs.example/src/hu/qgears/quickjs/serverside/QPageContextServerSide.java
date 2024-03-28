package hu.qgears.quickjs.serverside;

import hu.qgears.quickjs.qpage.IQPageContaierContext;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.serialization.RemotingBase;

/** The server side implementation of the page context. */
public class QPageContextServerSide implements IQPageContaierContext {
	private QPage page;
	public QPageContextServerSide(QPage page) {
		super();
		this.page = page;
	}
	@Override
	public Object getInitializeObject() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public RemotingBase openConnection(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getPageContextPath() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public QPage getPage() {
		return page;
	}
}
