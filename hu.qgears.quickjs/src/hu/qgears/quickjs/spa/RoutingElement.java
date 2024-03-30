package hu.qgears.quickjs.spa;

import hu.qgears.quickjs.qpage.QQueryWrapper;

public class RoutingElement {
	public IQueryFilter filter;
	public Routing then;
	RoutingEndpoint endpoint;
	public RoutingElement(IQueryFilter filter, Routing then, RoutingEndpoint endpoint) {
		super();
		this.filter = filter;
		this.then = then;
		this.endpoint=endpoint;
	}
	public boolean query(QQueryWrapper query, String path, int pathAt) {
		int consumePathChars=0;
		if(filter!=null)
		{
			Integer c=filter.match(query, path, pathAt);
			if(c==null)
			{
				return false;
			}
			consumePathChars=c;
		}
		pathAt+=consumePathChars;
		try
		{
			boolean ret=false;
			if(then!=null)
			{
				ret=then.query(query, path, pathAt);
				if(ret)
				{
					return true;
				}
			}
			if(endpoint!=null) {
				ret=query.executeEndpoint(endpoint, path, pathAt);
			}
			return ret;
		}finally
		{
			pathAt-=consumePathChars;
		}
	}
}
