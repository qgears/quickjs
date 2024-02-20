package hu.qgears.quickjs.utils;

import hu.qgears.quickjs.qpage.HtmlTemplate;

/**
 * Send Javascript web application logs to the server:
 *  * This class implements the Javascript required to send logs to the server.
 *  * LogHandler implements receiving the log messages and writing them to the web server's logs.
 */
public class ServerLoggerJs extends HtmlTemplate {
	private String windowReference="window";
	private String indexedCommAccessor;
	public void generateWs(HtmlTemplate parent, String indexedCommAccessor)
	{
		this.indexedCommAccessor=indexedCommAccessor;
		generate(parent, null);
	}
	/**
	 * Generate Jacascript code that initialize server side logging.
	 * Has to be included into the HTML page.
	 * @param parent template host to write output to.
	 * @param loggerCallbackPath path on which the {@link LogHandler} class is listening for returned logs.
	 */
	public void generate(HtmlTemplate parent, String loggerCallbackPath)
	{
		setParent(parent);
		write("/** Server side logger implementation: send all logs to a server\n *  so that it is possible to debug issues using server logs only.\n */\n\nserverlogger_collected=[];\n\n/// Global logging handler - all log events must call this function.\nfunction serverlogger_send(logObject)\n{\n if(logObject.args)\n {\n  const a=logObject.args;\n  for(var i=0;i<a.length;++i)\n  {\n    var v=a[i];\n    if(v instanceof Error)\n    {\n    \tconst newVal={type: v.type, message: v.message, stack: v.stack};\n    \ta[i]=newVal;\n    }\n   }\n }\n if(typeof serverlogger_timeout == 'undefined')\n {\n");
		if(indexedCommAccessor==null)
		{
			write("    serverlogger_timeout=setTimeout(function(){\n\t    fetch(\"");
			writeObject(loggerCallbackPath);
			write("\", {method: 'POST',body: JSON.stringify(serverlogger_collected),\n\t    \theaders: {\"Content-type\": \"application/json;charset=UTF-8\"}});\n\t\t\t  serverlogger_collected=[];\n\t\t\t  serverlogger_timeout=undefined;\n\t    }, 1000);\n");
		}else
		{
			write("    serverlogger_timeout=setTimeout(function(){\n    \t\ttry\n    \t\t{\n    \t\t\t");
			writeObject(indexedCommAccessor);
			write(".send({log: serverlogger_collected});\n\t\t\t\tserverlogger_collected=[];\n\t\t\t\tserverlogger_timeout=undefined;\n    \t\t}catch(e){\n    \t\t\tserverlogger_send(\"WS connection not ready yet\");\n    \t\t}\n\t    }, 1000);\n");
		}
		write(" }\n  logObject.timestamp=new Date();\n  serverlogger_collected.push(logObject);\n}\n\n{\n  const log = console.log.bind(console)\n  const info = console.info.bind(console)\n  const error = console.error.bind(console)\n  const warn = console.warn.bind(console)\n\n\n");
		writeObject(windowReference);
		write(".addEventListener('error', function (evt) {\n\tvar obj={};\n\tobj.type=\"error\";\n\tobj.message=evt.message;\n\tobj.filename=evt.filename;\n\tobj.lineno=evt.lineno;\n\tobj.colno=evt.colno;\n\tobj.args=[evt.error, \"Uncaught exception\"];\n\tserverlogger_send(obj);\n});\n  console.log = (...args) => {\n    var obj={};\n    obj.type=\"log\";\n    obj.args=args;\n    serverlogger_send(obj);\n    log(...args)\n  }\n  console.info = (...args) => {\n    var obj={};\n    obj.type=\"info\";\n    obj.args=args;\n    serverlogger_send(obj);\n    info(...args)\n  }\n  console.warn = (...args) => {\n    var obj={};\n    obj.type=\"warn\";\n    obj.args=args;\n    serverlogger_send(obj);\n    warn(...args)\n  }\n  console.error = (...args) => {\n    var obj={};\n    obj.type=\"error\";\n    obj.args=args;\n    serverlogger_send(obj);\n    error(...args);\n  }\n}\n\n");
		setParent(null);
	}
	/**
	 * Set the Javascript keyword to reference to the "window" object.
	 * Normally it is "window" and need not be overridden.
	 * It is used to access: "windowReference.addEventListener('error', function (evt) { ..."
	 * In case of a service worker this object is called "self" - this is the only code where overriding this variable is used now.
	 * 
	 * @param windowReference
	 * @return
	 */
	public ServerLoggerJs setWindowReference(String windowReference) {
		this.windowReference = windowReference;
		return this;
	}
}
