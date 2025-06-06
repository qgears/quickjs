package hu.qgears.quickjs.qpage;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.quickjs.helpers.QTimer;

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
	protected QPageContainer page;
	protected String id;
	private List<QComponent> children=new ArrayList<>();
	private boolean disposed;
	private IQContainer container;
	public final UtilListenableProperty<Object> userObject=new UtilListenableProperty<>();
	private Map<String, Object> userObjectStorage;
	private UtilEvent<QComponent> initEvent;
	private UtilEvent<JSONObject> userEvent;
	private String controlledNodeSelector;
	private String childContainerSelector;
	private DisposableContainer disposableContainer;
	protected boolean disabled;
	private UtilListenableProperty<Point> size;
	private static Logger log=LoggerFactory.getLogger(QComponent.class);
	/**
	 * Focused event. In case a listener is added then onfocus listening is activated.
	 */
	@SuppressWarnings("resource")
	public final UtilEventWithListenerTrack<QComponent> focused=new UtilEventWithListenerTrack<>(e->{
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
	});

	public QComponent(IQContainer container, String id) {
		super();
		if(id==null && container!=null)
		{
			id=container.getPageContainer().createComponentId();
		}
		this.id = id;
		this.container=container;
		if(container!=null)
		{
			this.page = container.getPageContainer();
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
		this(container, container==null?null:container.getPageContainer().createComponentId());
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

	/**
	 * Must set "inited" field to true!
	 * @param parent
	 */
	final public void init()
	{
		if(page!=null)
		{
			try(NoExceptionAutoClosable c=activateJS())
			{
				doInitJSObject();
			}
		}
		initChildren();
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
	public QPageContainer getPageContainer() {
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
						write(getPageContainer().getPlatform().loadResource(name));
					}
				} catch (Exception e) {
					log.error("Load additional component JS "+getClass().getName(), e);
				}
				write("</script>\n");
			}
		}.generate();
	}
	public List<String> getScriptReferences() {
		return Collections.singletonList(getClass().getSimpleName()+".js");
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
			disposed=true;
			if(disposableContainer!=null)
			{
				disposableContainer.close();
			}
		}
	}
	private void removeFromParentList()
	{
		container.removeChild(this);
	}
	@Override
	public void removeChild(QComponent child) {
		children.remove(child);
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
	public String[] getConponentJsFiles()
	{
		return new String[] {};
	}	
	public void styleAddClass(final String msg) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].styleAddClass(\"");
			writeJSValue(msg);
			write("\");\n");
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
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].styleRemoveClass(\"");
			writeJSValue(msg);
			write("\");\n");
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
	public void handleClientPost(Msg msg, JSONObject post) throws IOException {
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
		if(childContainerSelector!=null)
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
		if(controlledNodeSelector!=null)
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
		String id=getPageContainer().createComponentId();
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
	private DisposableContainer getDisposableContainer()
	{
		if(disposableContainer==null)
		{
			disposableContainer=new DisposableContainer();
			if(disposed)
			{
				disposableContainer.close();
			}
		}
		return disposableContainer;
	}
	@Override
	public NoExceptionAutoClosable addCloseable(AutoCloseable closeable) {
		return getDisposableContainer().addCloseable(closeable);
	}
	@Override
	public NoExceptionAutoClosable addOnClose(Runnable closeable) {
		return getDisposableContainer().addOnClose(closeable);
	}
	public void setDisabled(final boolean disabled) {
		this.disabled=disabled;
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("page.components['");
			writeJSValue(id);
			write("'].setDisabled(");
			writeObject(disabled);
			write(");\n");
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
			startSizeListener();
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
	/** Create and start a timer.
	 * The created timer is added to this QComponent's disposable so it is auto-closed once the component is disposed.
	 * @param r task to run when timeout happens. Exceptions are caught and logged to logger.
	 * @param firstTimeoutMs first timeout in ms.
	 * @param periodMs 0 means no periodic restart of the timer
	 */
	public QTimer startTimer(Runnable r, int firstTimeoutMs, int periodMs) {
		QTimer t=getPageContainer().getPlatform().startTimer(r, firstTimeoutMs, periodMs);
		addCloseable(t);
		return t;
	}
	public void disposeAllChildren() {
		QComponent[] comps=children.toArray(new QComponent[] {});
		for(QComponent c: comps)
		{
			c.dispose();
		}
	}
	public void scrollIntoView()
	{
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tpage.components[\"");
			writeObject(id);
			write("\"].scrollIntoView();\n");
		}
	}
}
