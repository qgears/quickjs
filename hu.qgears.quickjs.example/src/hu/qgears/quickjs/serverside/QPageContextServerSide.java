package hu.qgears.quickjs.serverside;

import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.helpers.PromiseImpl;
import hu.qgears.quickjs.qpage.IQPageContaierContext;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.serialization.RemotingBase;

/** The server side implementation of the page context. */
public class QPageContextServerSide implements IQPageContaierContext {
	private QPage page;
	private String pageContextPath;
	public QPageContextServerSide(QPage page, String pageContextPath) {
		super();
		this.page = page;
		this.pageContextPath = pageContextPath;
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
		return pageContextPath;
	}
	@Override
	public QPage getPage() {
		return page;
	}
	@Override
	public Promise<String> getResourcePath(String resourceId) {
		String retv=pageContextPath+"/"+resourceId;
		PromiseImpl<String> ret=new PromiseImpl<>();
		ret.ready(retv);
		return ret;
	}
	@Override
	public String getResourcePathSync(String resourceId) {
		return getResourcePath(resourceId).getValueSync();
	}
}
