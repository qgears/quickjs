package hu.qgears.quickjs.spa;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.quickjs.qpage.QQueryWrapper;

/** Routing state of parsing a path and finding the handler. */
public class Routing {
	private List<RoutingElement> elements=new ArrayList<>();
	public Routing method(String method)
	{
		Routing ret=new Routing();
		RoutingElement re=new RoutingElement((q, path, pathAt)->method.equals(q.getMethod())?0:null, ret, null);
		elements.add(re);
		return ret;
	}
	public void handle(RoutingEndpoint ep)
	{
		RoutingElement re=new RoutingElement(null, null, ep);
		elements.add(re);
	}
	public boolean query(QQueryWrapper query, String path, int pathAt)
	{
		for(RoutingElement re: elements)
		{
			if(re.query(query, path, pathAt))
			{
				return true;
			}
		}
		return false;
	}
	public Routing path(String pathElement) {
		boolean endsWithSlash=pathElement.lastIndexOf('/')==pathElement.length()-1;
		Routing ret=new Routing();
		RoutingElement re=new RoutingElement((q, path, pathAt)->{
			if(path.startsWith(pathElement, pathAt))
			{
				if(endsWithSlash)
				{
					return pathElement.length();
				}
				if(path.length()==pathAt+pathElement.length())
				{
					return pathElement.length();
				}else if(path.charAt(pathAt+pathElement.length())=='/')
				{
					return pathElement.length();
				}
			}
			return null;
		}, ret, null);
		elements.add(re);
		return ret;
	}
}
