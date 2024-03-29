package hu.qgears.quickjs.qpage;
/** Message from JavaScript to Java (in browser TeaVM or on server) */
public class Msg
{
	/**
	 * {@link JSONObject} or string
	 */
	public Object header;
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
