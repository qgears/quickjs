package hu.qgears.quickjs.qpage;

import org.json.JSONObject;

/** Message from JavaScript to Java (in browser TeaVM or on server) */
public class Msg
{
	public JSONObject header;
	public long index;
	public Object[] arguments;
	public int nPart;
	public int currentArgument;
	public void log()
	{
		System.out.println("Header: "+header);
		for(int i=0;i<arguments.length;++i)
		{
			System.out.println("Arg "+i+" "+arguments[i]);
		}
	}
}
