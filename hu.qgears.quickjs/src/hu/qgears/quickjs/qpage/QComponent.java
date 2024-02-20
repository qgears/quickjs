package hu.qgears.quickjs.qpage;

import java.awt.Point;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.quickjs.qpage.IndexedComm.Msg;

/**
 * When a Java/JS component is created it is either already in the HTML tree
 * or a creator must be defined that creates the object in the web page.
 * 
 * All components have 3 nodes (which may be the same):
 *  * Root node - this is where the object "starts" and this is deleted when the object is disposed
 *  * childContainer (must be within the root and by default is the root): this is where new child objects are added to
 *  * controlledNode (must be within the root and by default is the root): this is where self content is updated for components that handle content - example: button, label
 */
public abstract class QComponent extends HtmlTemplate implements IQContainer, IUserObjectStorage
{
	protected QPage page;
	protected String id;
	@Deprecated
	protected boolean inited;
	private List<QComponent> children=new ArrayList<>();
	private boolean disposed;
	private IQContainer container;
	private ComponentCreator creator;
	public final UtilListenableProperty<Object> userObject=new UtilListenableProperty<>();
	private Map<String, Object> userObjectStorage;
	private UtilEvent<QComponent> initEvent;
	private UtilEvent<JSONObject> userEvent;
	private String controlledNodeSelector;
	private String childContainerSelector;
	private List<AutoCloseable> closeables;
	private Set<String> styleToAdd, styleToRemove;
	protected boolean disabled;
	private UtilListenableProperty<Point> size;
	/**
	 * Focused event. In case a listener is added then onfocus listening is activated.
	 */
	@SuppressWarnings("resource")
	public final UtilEventWithListenerTrack<QComponent> focused=new UtilEventWithListenerTrack<>(e->{
		if(inited)
		{
			if(e.getNListeners()==1)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					write("\tpage.components[\"");
					writeObject(id);
					write("\"].addFocusListener(true);\n");
				}
			}else if(e.getNListeners()==0)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					write("\tpage.components[\"");
					writeObject(id);
					write("\"].addFocusListener(false);\n");
				}
			}
		}
	});

	public QComponent(IQContainer container, String id) {
		super();
		if(id==null && container!=null)
		{
			id=container.getPage().createComponentId();
		}
		this.id = id;
		this.container=container;
		if(container!=null)
		{
			this.page = container.getPage();
		}
		if(page!=null)
		{
			page.add(this);
			// TODO remove this feature totally
			// page.registerToInit(this);
			if(!isSelfInitialized())
			{
				init();
			}
		}
		if(container!=null)
		{
			container.addChild(this);
		}
	}
	/**
	 * Create a component object with auto-generated unique identifier
	 * @param container
	 */
	public QComponent(IQContainer container) {
		this(container, container.getPage().createComponentId());
	}
	/**
	 * Create a component object with given identifier
	 * @param id
	 */
	public QComponent(String id) {
		this(QPage.getCurrent(), id);
	}
	/**
	 * Create a component object with auto-generated unique identifier
	 * and using QPage.getCurrent() as parent.
	 */
	public QComponent() {
		this(QPage.getCurrent(), null);
	}

	public void generateHtmlObject(HtmlTemplate templateTarget)
	{
		try(ResetOutputObject eoo=setParent(templateTarget))
		{
			generateHtmlObject();
		}
	}
	abstract public void generateHtmlObject();

	/**
	 * Must set "inited" field to true!
	 * @param parent
	 */
	final public void init()
	{
		if(!inited && page!=null)
		{
			try(NoExceptionAutoClosable c=activateJS())
			{
				doCreateHTMLObject();
				doInitJSObject();
				if(this.childContainerSelector!=null)
				{
					syncChildContainerSelector();
				}
			}
			inited=true;
			if(styleToAdd!=null)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					for(String s: styleToAdd)
					{
						write("\tpage.components[\"");
						writeObject(id);
						write("\"].styleAddClass(\"");
						writeJSValue(s);
						write("\");\n");
					}
				}
				styleToAdd=null;
			}
			if(styleToRemove!=null)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					for(String s: styleToRemove)
					{
						write("\tpage.components[\"");
						writeObject(id);
						write("\"].styleRemoveClass(\"");
						writeJSValue(s);
						write("\");\n");
					}
				}
				styleToRemove=null;
			}
			if(focused.getNListeners()>0)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					write("\tpage.components[\"");
					writeObject(id);
					write("\"].addFocusListener(true);\n");
				}
			}
			if(size!=null)
			{
				startSizeListener();
			}
			if(initEvent!=null)
			{
				initEvent.eventHappened(this);
			}
		}
		initChildren();
	}
	/**
	 * Create HTML object into the HTML tree.
	 * This is called in the init phase of the component.
	 * In case of pre-created HTML objects
	 * (when the HTML tree of the object is already in the DOM tree and we only
	 *  create and connect the JS object to the existing DOM in the init phase)
	 * this step is a no-op.
	 */
	protected void doCreateHTMLObject()
	{
		if(creator!=null)
		{
			creator.createHtmlFor(this);
		}
	}

	abstract protected void doInitJSObject();
	/**
	 * Handle incoming message. This version can contain (binary) attachments not only the 
	 * mandatory JSON.
	 * @param parent
	 * @param msg
	 */
	protected void handle(Msg msg)
	{
		JSONObject o=((JSONObject)msg.header);
		if(o.has("type"))
		{
			switch(o.getString("type"))
			{
			case "onfocus":
				focused.eventHappened(this);
				break;
			case "size":
			{
				Point s=new Point(
						(int)((JSONObject)msg.header).optDouble("clientWidth", 0.0),
						(int)((JSONObject)msg.header).optDouble("clientHeight", 0.0));
				size.setProperty(s);
				break;
			}
			}
		}
	}
	abstract public void handle(JSONObject post) throws IOException;

	final public String getId() {
		return id;
	}
	public QPage getPage() {
		return page;
	}

	public void generateHeader(HtmlTemplate parent)
	{
		new HtmlTemplate(parent){

			public void generate() {
				write("<script language=\"javascript\" type=\"text/javascript\">\n");
				try {
					for(String name: getScriptReferences())
					{
						write(new String(loadJs(name), StandardCharsets.UTF_8));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				write("</script>\n");
			}
			
		}.generate();
	}

	public byte[] loadJs(String name) throws IOException {
		if(name.equals(getClass().getSimpleName()))
		{
			try {
				return UtilFile.loadFile(getClass().getResource(name+".js"));
			} catch (Exception e) {
				System.err.println("LOADJS: "+getClass().getName()+" "+name);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public List<String> getScriptReferences() {
		return Collections.singletonList(getClass().getSimpleName());
	}
	/**
	 * Delete the object from the server side registry and also delete from the
	 * runtime side.
	 * 
	 * Can not be overridden. Extra tasks must be executed in onDispose() in subclasses.
	 */
	final public void dispose() {
		disposePrivate(true);
	}
	final protected void disposePrivate(boolean removeFromParent)
	{
		if(!disposed)
		{
			try
			{
				onDispose();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			for(QComponent c: children)
			{
				c.disposePrivate(false);
			}
			children.clear();
			if(removeFromParent)
			{
				removeFromParentList();
			}
			if(page.getJsTemplate()!=null)
			{
				try(NoExceptionAutoClosable c=activateJS())
				{
					write("\tpage.getComponent(\"");
					writeJSValue(id);
					write("\").dispose();\n");
				}
			}
			page.remove(this);
			if(closeables!=null)
			{
				for(AutoCloseable c: closeables)
				{
					closeCloseable(c);
				}
			}
			closeables=null;
		}
		disposed=true;
	}
	private void removeFromParentList()
	{
		if(container instanceof QComponent)
		{
			QComponent p=(QComponent) container;
			p.children.remove(this);
		}
	}
	/**
	 * Called when the object is deleted from the server.
	 * Either by the code or by the page closed in the browser.
	 */
	protected void onDispose() {
	}
	/**
	 * Add a child to this object in the server side hierarchy.
	 * Children are automatically disposed when the parent is disposed.
	 * @param child
	 */
	@Override
	public void addChild(QComponent child) {
		testNotDisposed();
		Objects.requireNonNull(child);
		children.add(child);
	}
	/**
	 * Throw exception in case of this component is disposed.
	 */
	protected void testNotDisposed() {
		if(disposed){
			throw new RuntimeException("Component already disposed.");
		}
	}

	public void initChildren() {
		for(QComponent c: children)
		{
			c.init();
		}
	}
	@Override
	public IQContainer getParent() {
		return container;
	}
	/**
	 * Set the creator that creates the HTML node for this object.
	 * @param creator
	 */
	public void setCreator(ComponentCreator creator) {
		this.creator = creator;
	}
	/**
	 * Set up whether the HTML f this component already exists in the HTML tree or
	 * it has to be created when the object is created.
	 * @param existing true means already existing HTML DOM objects and sets creator to null
	 *        false means HTML DOM must be created and sets up the default creator that will 
	 *        call the doCreateHTMLObject() function before initializing the JS object.
	 * @return this same object - for possible command chaining
	 */
	public QComponent setHtmlExisting(boolean existing)
	{
		if(!existing)
		{
			setCreator(page.defaultCreator);
		}else
		{
			setCreator(null);
		}
		return this;
	}

	public void registerResources(Map<String, URL> jsResources) {
		for(String s: getScriptReferences())
		{
			jsResources.put(s+".js", getClass().getResource(s+".js"));
		}
	}
	
	public void styleAddClass(final String msg) {
		if(inited)
		{
			try(NoExceptionAutoClosable c=activateJS())
			{
				write("page.components['");
				writeJSValue(id);
				write("'].styleAddClass(\"");
				writeJSValue(msg);
				write("\");\n");
			}
		}else
		{
			if(styleToAdd==null)
			{
				styleToAdd=new TreeSet<>();
			}
			styleToAdd.add(msg);
			if(styleToRemove!=null)
			{
				styleToRemove.remove(msg);
			}
		}
	}
	public void setStyle(final String style, boolean value)
	{
		if(value)
		{
			styleAddClass(style);
		}else
		{
			styleRemoveClass(style);
		}
	}
	public void setDynamicStyleValue(final String styleKey, String value)
	{
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].setDynamicStyle(\"");
			writeJSValue(styleKey);
			write("\",\"");
			writeJSValue(value);
			write("\");\n");
		}
	}
	public void styleRemoveClass(final String msg) {
		if(inited)
		{
			try(NoExceptionAutoClosable c=activateJS())
			{
				write("page.components['");
				writeJSValue(id);
				write("'].styleRemoveClass(\"");
				writeJSValue(msg);
				write("\");\n");
			}
		}else
		{
			if(styleToRemove==null)
			{
				styleToRemove=new TreeSet<>();
			}
			styleToRemove.add(msg);
			if(styleToAdd!=null)
			{
				styleToAdd.remove(msg);
			}
		}
	}
	/**
	 * Get current children of this component.
	 * @return The returned list is a copy of the current internal state so it can be freely changed
	 *   or iterated while this object is being changed.
	 */
	public List<QComponent> getChildren() {
		return new ArrayList<QComponent>(children);
	}
	/**
	 * Query whether this object is already disposed.
	 * @return
	 */
	public boolean isDisposed() {
		return disposed;
	}
	@Override
	synchronized public Map<String, Object> getUserObjectStorage() {
		if(userObjectStorage==null)
		{
			userObjectStorage=new HashMap<String, Object>();
		}
		return userObjectStorage;
	}
	public UtilEvent<QComponent> getInitEvent() {
		if(initEvent==null)
		{
			initEvent=new UtilEvent<>();
		}
		return initEvent;
	}
	/**
	 * 
	 * @return Received events are JSON encoded.
	 */
	public UtilEvent<JSONObject> getUserEvent() {
		if(userEvent==null)
		{
			userEvent=new UtilEvent<>();
		}
		return userEvent;
	}
	public void handleClientPost(IndexedComm.Msg msg, JSONObject post) throws IOException {
		if(post.has("user"))
		{
			if(userEvent!=null)
			{
				userEvent.eventHappened(post.getJSONObject("user"));
			}
		}else
		{
			handle(msg);
			handle(post);
		}
	}
	/**
	 * In case this object has a different child container than the root DOM object then 
	 * use this method to set a selector for this child.
	 * This selector is executed within the root DOM object and the first found element
	 * will be set as child container - if that exists.
	 * Example ":scope > .foo" means that only first level children are searched
	 * @return
	 */
	public QComponent setChildContainerSelector(String childContainerSelector)
	{
		this.childContainerSelector=childContainerSelector;
		if(childContainerSelector!=null && this.inited)
		{
			syncChildContainerSelector();
		}
		return this;
	}
	/**
	 * In case this object has a different node controlled than the root DOM object then 
	 * use this method to set a selector for this child.
	 * This selector is executed within the root DOM object and the first found element
	 * will be set as child container - if that exists.
	 * @return
	 */
	public QComponent setControlledNodeSelector(String controlledNodeSelector)
	{
		this.controlledNodeSelector=controlledNodeSelector;
		if(controlledNodeSelector!=null && this.inited)
		{
			syncControlledNodeSelector();
		}
		return this;
	}
	private void syncControlledNodeSelector()
	{
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tpage.components[\"");
			writeJSValue(getId());
			write("\"].setControlledNodeSelector(\"");
			writeJSValue(controlledNodeSelector);
			write("\");\n");
		}
	}
	private void syncChildContainerSelector() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tpage.components[\"");
			writeJSValue(getId());
			write("\"].setChildContainerSelector(\"");
			writeJSValue(childContainerSelector);
			write("\");\n");
		}
	}
	/**
	 * Create a child container id set it as child container selector
	 * and return this newly created id.
	 * @return
	 */
	public String createChildContainerId() {
		String id=getPage().createComponentId();
		setChildContainerSelector("#"+id);
		return id;
	}
	/**
	 * Change the parent of this node.
	 *  * Remove from current parent in container hierarchy Java side.
	 *  * Add to new parent in container hierarchy Java side.
	 *  * Move DOM from current place to new container's content DOM node in JS side.
	 * @param newParent
	 * @param selector within the container DOM element. empty string selects the container node
	 */
	public void setParent(QComponent newParent, String selector, int index)
	{
		if(newParent.isDisposed() || isDisposed())
		{
			throw new IllegalStateException("This or new parent is already disposed.");
		}
		removeFromParentList();
		newParent.children.add(this);
		container=newParent;
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tpage.components[\"");
			writeJSValue(getId());
			write("\"].setParent(page.components[\"");
			writeJSValue(newParent.getId());
			write("\"], \"");
			writeJSValue(selector);
			write("\", ");
			writeObject(index);
			write(");\n");
		}
	}
	@Override
	public void addCloseable(AutoCloseable closeable) {
		if(isDisposed())
		{
			closeCloseable(closeable);
		}else
		{
			if(closeables==null)
			{
				closeables=new ArrayList<>();
			}
			closeables.add(closeable);
		}
	}
	private void closeCloseable(AutoCloseable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {
			Logger.getLogger(getClass()).error("Closing attached closeable", e);
		}
	}
	public void setDisabled(final boolean disabled) {
		this.disabled=disabled;
		if(inited)
		{
			try(NoExceptionAutoClosable c=activateJS())
			{
				write("page.components['");
				writeJSValue(id);
				write("'].setDisabled(");
				writeObject(disabled);
				write(");\n");
			}
		}
	}
	/**
	 * To be called from doInitJS()
	 */
	protected void initialDisabledState() {
		write(".setDisabled(");
		writeObject(disabled);
		write(")");
	}
	
	private void startSizeListener()
	{
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tpage.components[\"");
			writeObject(id);
			write("\"].addSizeListener(true);\n");
		}
	}
	public UtilListenableProperty<Point> listenSize() {
		if(size==null)
		{
			size=new UtilListenableProperty<>();
			if(inited)
			{
				startSizeListener();
			}
		}
		return size;
	}
	/**
	 * Means init() is called by subclass constructor. 
	 * All must be true and then removed.
	 * @return
	 */
	@Deprecated
	protected boolean isSelfInitialized()
	{
		return false;
	}
}
