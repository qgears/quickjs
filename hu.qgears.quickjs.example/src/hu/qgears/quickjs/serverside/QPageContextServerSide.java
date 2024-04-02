package hu.qgears.quickjs.serverside;

import java.util.HashMap;
import java.util.Map;

import hu.qgears.commons.UtilEvent;
import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.helpers.PromiseImpl;
import hu.qgears.quickjs.qpage.IQPageContaierContext;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.serialization.IRemotingBase;

/** The server side implementation of the page context. */
public class QPageContextServerSide implements IQPageContaierContext {
	public QPage page;
	public String pageContextPath;
	public Object initializeObject;
	protected String cacheParameter;
	public Map<String, IRemotingBase> connections=new HashMap<>();
	public final UtilEvent<QPageContextServerSide> closeEvent=new UtilEvent<>();
	private IImageSizeToUrl imageSizeToUrl=null;
	public QPageContextServerSide(QPage page, String pageContextPath) {
		super();
		this.page = page;
		this.pageContextPath = pageContextPath;
	}
	@Override
	public Object getInitializeObject() {
		return initializeObject;
	}
	@Override
	public IRemotingBase openConnection(String id) {
		return connections.get(id);
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
		String retv=pageContextPath+"/"+resourceId+"?"+cacheParameter;
		PromiseImpl<String> ret=new PromiseImpl<>();
		ret.ready(retv);
		return ret;
	}
	@Override
	public String getResourcePathSync(String resourceId) {
		return getResourcePath(resourceId).getValueSync();
	}
	public void setCacheParameter(String string) {
		this.cacheParameter=string;
	}
	@Override
	public String getImagePathSync(String s) {
		if(imageSizeToUrl!=null)
		{
			return imageSizeToUrl.imageSizeToUrl(s, cacheParameter, null);
		}
		return getResourcePathSync(s);
	}
	@Override
	public String getImagePathSync(String s, int requiredWidth) {
		if(imageSizeToUrl!=null)
		{
			return imageSizeToUrl.imageSizeToUrl(s, cacheParameter, requiredWidth);
		}
		return getResourcePathSync(s);
	}
	public void setImageSizeToUrl(IImageSizeToUrl imageSizeToUrl) {
		this.imageSizeToUrl = imageSizeToUrl;
	}
}
