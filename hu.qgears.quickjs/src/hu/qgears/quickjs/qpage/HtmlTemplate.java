package hu.qgears.quickjs.qpage;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import hu.qgears.commons.EscapeString;
import hu.qgears.commons.NoExceptionAutoClosable;

/**
 * Template to output HTML content
 */
public class HtmlTemplate {
	protected Writer out;
	private List<Object> webSocketArguments;
	private boolean isTextFirstArgument=false;
	private volatile ILocalizationInterface locIf;
	private static Supplier<ILocalizationInterface> localizeFinder=()->new ILocalizationInterface() {};
	public class ResetOutputObject implements NoExceptionAutoClosable
	{
		private Writer resetTo;
		private List<Object> resetToWebSocketArguments;
		private ResetOutputObject chained;
		public ResetOutputObject(Writer resetTo, List<Object> resetToWebSocketArguments) {
			super();
			this.resetTo = resetTo;
			this.resetToWebSocketArguments=resetToWebSocketArguments;
		}
		@Override
		public void close()
		{
			if(chained!=null)
			{
				chained.close();
			}
			out=resetTo;
			webSocketArguments=resetToWebSocketArguments;
		}
	}
	public HtmlTemplate()
	{
		init();
	}
	public HtmlTemplate(HtmlTemplate parent) {
		this.out=parent.out;
		webSocketArguments=parent.webSocketArguments;
		locIf=parent.getLocalizationSafe();
		init();
	}
	public HtmlTemplate(Writer out) {
		this.out=out;
		init();
	}
	private void init() {
	}
	/**
	 * Set the parent template. All further output will be written to the parent template.
	 * @param parent parent template or null. Null means that further output is not allowed.
	 */
	protected ResetOutputObject setParent(HtmlTemplate parent)
	{
		ResetOutputObject ret=new ResetOutputObject(out, webSocketArguments);
		if(parent!=null)
		{
			this.webSocketArguments=parent.webSocketArguments;
			this.out=parent.getWriter();
			this.locIf=parent.getLocalizationSafe();
		}else
		{
			this.out=null;
			this.webSocketArguments=null;
		}
		return ret;
	}
	/**
	 * Write a value through an escape HTML filter so HTML tags and any kind of string
	 * will be shown to the user in its original form.
	 * @param value
	 */
	protected void writeHtml(String value) {
		if(value!=null)
		{
			try {
				EscapeString.escapeHtml(out, value);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	/**
	 * Write value through a JavaScript escaping filter.
	 * @param text
	 */
	protected void writeJSValue(String text) {
		try {
			EscapeString.escapeJavaScript(out, text);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Write string through a HTML attribute value filter.
	 * @param key
	 */
	protected void writeValue(String key) {
		try {
			EscapeString.escapeHtml(out, key);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Write the object as a raw string without any filter.
	 * @param o
	 */
	protected void writeObject(Object o) {
		try {
			out.write(""+o);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Write the template string raw without any filter.
	 * @param s
	 */
	protected void write(String s) {
		try {
			out.write(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Write the translated/localized version of a string by key.
	 * Arguments are written into the localized string with formatting required by the translation variable (number of digits, format of date, etc). The formatting is also localizable.
	 * When writing HTML the translated version is also escaped for HTML.
	 * TODO not implemented yet
	 * @param key the key of the translatable string
	 * @param args arguments to fill into the translatable string
	 */
	protected void writeTranslation(String key, Object... args)
	{
		writeHtml(getTranslation(key, args));
	}
	protected ILocalizationInterface getLocalizationSafe()
	{
		if(locIf==null)
		{
			try {
				locIf=localizeFinder.get();
			} catch (Exception e) {
				// Ignore
			}
			if(locIf==null)
			{
				locIf=new ILocalizationInterface() {
				};
			}
		}
		return locIf;
	}
	public String getTranslation(String key, Object... args) {
		String s=getLocalizationSafe().getString(key, args);
		return s;
	}
	public String formatDate(Date date) {
		if(date!=null)
		{
			String s=getLocalizationSafe().formatDate(date);
			return s;
		}else
		{
			return "null";
		}
	}
	public String formatDateLong(Date date) {
		String s=getLocalizationSafe().formatDateLong(date);
		return s;
	}
	/**
	 * Access the writer object of this template.
	 * Useful when a child template has to be configured to write into this template.
	 * @return
	 */
	public Writer getWriter() {
		return out;
	}
	/**
	 * Set the current writer output of this template.
	 * @param out
	 * @return the previous writer of this template.
	 */
	public Writer setWriter(Writer out) {
		Writer prev=this.out;
		this.out = out;
		return prev;
	}
	/**
	 * Add an additional object to the current message.
	 * Must be a String or a Blob compatible object(byte[], INativeMemory).
	 * In case of a {@link Closeable} the object is closed when send was acknowledged by the client.
	 * 
	 * @return Index of the stored object counted from 0. Can be used to access the object on the JS side.
	 */
	public int additionalObject(Object o)
	{
		if(webSocketArguments==null)
		{
			webSocketArguments=new ArrayList<>();
		}
		webSocketArguments.add(o);
		return webSocketArguments.size()-1;
	}
	public Object[] toWebSocketArguments() {
		if(isTextFirstArgument)
		{
			webSocketArguments.set(0, getWriter().toString());
		}
		if(webSocketArguments!=null)
		{
			return webSocketArguments.toArray();
		}else
		{
			return new Object[] {};
		}
	}
	/**
	 * Set up the web socket arguments collection feature.
	 * This feature allows generated JS code to send websocket blob and string messages separately
	 * of the JS itself.
	 * This is useful because blob data need not be encoded into a string but it is sent as
	 * binary data efficiently (PNG data can be an example).
	 * Usage of the feature:
	 *  int index=additionalData(pngAsByteArray);
	 *  And in the JS the argument can be accessed by this array and the index returned from additionalData: 
	 *  setImageAsBlob(args[index]);
	 * @param isTextFirstArgument arguments are indexed from 1 (otherwise from 0).
	 *                    if true then toWebSocketArguments will add the writer content to the object array
	 * @return
	 */
	public HtmlTemplate setupWebSocketArguments(boolean isTextFirstArgument) {
		webSocketArguments=new ArrayList<>();
		if(isTextFirstArgument)
		{
			webSocketArguments.add(null);
		}
		this.isTextFirstArgument=isTextFirstArgument;
		return this;
	}
	/**
	 * Write a localized string to the output through HTML encoding
	 * @param id
	 * @param args parameters of a parametrized string output
	 */
	final protected void writeLocalized(String id, Object... args)
	{
		String s=getLocalizationSafe().getString(id, args);
		writeHtml(s);
	}
	public static void setLocalizationSupplier(Supplier<ILocalizationInterface> localizeFinder) {
		HtmlTemplate.localizeFinder = localizeFinder;
	}
	protected String escapeUrlPart(String expressionString, char limitchar) {
		return expressionString;
	}
	/**
	 * Template output will be treated as JS and will be executed in order of appearance on the
	 * server.
	 * If the JS block is opened inside an activateCreateDom block then the JS
	 * will be executed in the browser _after_ the DOM objects are already inserted into
	 * the DOM tree of the browser.
	 * @return
	 */
	protected NoExceptionAutoClosable activateJS() {
		ResetOutputObject roo=setParent(QPage.getCurrent().getParent().getJsTemplate());
		return new NoExceptionAutoClosable() {
			@Override
			public void close() {
				roo.close();
			}
		};
	}
	/**
	 * Template output will be treated as HTML and the resulting HTML will be
	 * inserted into the DOM tree at the appendTarget value.
	 * All JS that is emitted inside the activateCreateDom block will be executed _after_ the DOM node was created.
	 * @param appendTarget specification where to append the created HTML.
	 * @return has to be closed to deactivate create DOM mode and actually create the HTML node into DOM
	 */
	protected NoExceptionAutoClosable activateCreateDom(AppendTarget appendTarget) {
		HtmlTemplate template=new HtmlTemplate(new StringWriter());
		HtmlTemplate subJs=new HtmlTemplate(new StringWriter());
		QPageContainer page=QPage.getCurrent().getParent();
		HtmlTemplate originalJsTemplate=page.getJsTemplate();
		subJs.webSocketArguments=originalJsTemplate.webSocketArguments;
		page.setJsTemplate(subJs);
		ResetOutputObject roo=setParent(template);
		return new NoExceptionAutoClosable() {
			 @Override
			public void close() {
				page.setJsTemplate(originalJsTemplate);
				roo.close();
				String html=template.getWriter().toString();
				try(NoExceptionAutoClosable c=activateJS())
				{
						int index=additionalObject(html);
						write("page.createDom(args[");
						writeObject(index);
						write("], '");
						writeJSValue(appendTarget.nameSpaceUri);
						write("', '");
						writeJSValue(appendTarget.rootObjectType);
						write("', '");
						writeJSValue(appendTarget.method.name());
						write("', '");
						writeJSValue(appendTarget.domSelector);
						write("', '");
						writeJSValue(appendTarget.arg1);
						write("', '");
						writeJSValue(appendTarget.arg2);
						write("');\n");
						StringWriter sw=(StringWriter)subJs.getWriter();
						if(sw.getBuffer().length()>0)
						{
							writeObject(sw.toString());
						}
				 }
			}
		};
	}
}
