package hu.qgears.quickjs.utils;

import java.util.List;

import javax.servlet.ServletRequest;

import hu.qgears.commons.UtilString;

/**
 * Parse a path into its parts.
 * Also remember whether it is a folder query (ends with / ) or a file query(does not end with /)
 */
public class HttpPath {
	private static final String key=HttpPath.class.getName();
	private List<String> pieces;
	private boolean folder;
	public HttpPath(List<String> pieces, boolean folder) {
		super();
		this.pieces = pieces;
		this.folder = folder;
	}
	public HttpPath(String target)
	{
		pieces=UtilString.split(target, "/");
		folder=target.endsWith("/");
	}
	public List<String> getPieces() {
		return pieces;
	}
	public HttpPath getSubPath(int n)
	{
		return new HttpPath(pieces.subList(n, pieces.size()), folder);
	}
	public String toStringPath() {
		StringBuilder ret=new StringBuilder();
		boolean first=true;
		for(String s: pieces)
		{
			if(!first)
			{
				ret.append('/');
			}
			ret.append(s);
			first=false;
		}
		if(folder)
		{
			ret.append('/');
		}
		return ret.toString();
	}
	/**
	 * each part starts with / and a / is added in case it is a folder path.
	 * (the only way it does not start with / is that if it has 0 parts and it is not a folder.)
	 * @return
	 */
	public String toStringPathStartWithSlash() {
		StringBuilder ret=new StringBuilder();
		for(String s: pieces)
		{
			ret.append('/');
			ret.append(s);
		}
		if(folder)
		{
			ret.append('/');
		}
		return ret.toString();
	}
	public String getExtension() {
		if(folder|| pieces.size()==0)
		{
			return null;
		}
		String last=pieces.get(pieces.size()-1);
		int n=last.lastIndexOf('.');
		if(n>=0)
		{
			return last.substring(n+1);
		}
		return null;
	}
	/**
	 * Does it end with /?
	 * @return
	 */
	public boolean isFolder() {
		return folder;
	}
	public void setCurrent(ServletRequest req) {
		req.setAttribute(key, this);
	}
	public static HttpPath getCurrent(ServletRequest req) {
		return (HttpPath)req.getAttribute(key);
	}
}
