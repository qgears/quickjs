package hu.qgears.quickjs.qpage;

/** Specify the target where a newly created DOM subtree is put into the
 * DOM tree of the application.
 */
public class AppendTarget {
	/** String that selects the target node */
	public String domSelector;
	/** Append method. */
	public EAppendTargetMethod method;
	public String arg1;
	public String arg2;
	/** Namespace of the nodes to be created. Default is HTML and SVG, MathML etc are possible. */
	public String nameSpaceUri=namespaceUriHtml;
	/** Namespace of the node type that can contain the created elements. This node is used as a technical temporary container
	 *  when the subtree is created. */
	public String rootObjectType="div";
	public static final String namespaceUriHtml="http://www.w3.org/1999/xhtml";
	public static final String namespaceUriSvg="http://www.w3.org/2000/svg";
	public static final String namespaceUriMathML="http://www.w3.org/1998/Math/MathML";
	private AppendTarget() {}
	public static AppendTarget QContainer(QDiv dynamicContainer) {
		AppendTarget ret=new AppendTarget();
		ret.method=EAppendTargetMethod.QContainer;
		ret.domSelector=dynamicContainer.getId();
		return ret;
	}
	public static AppendTarget QContainer(IQContainer dynamicContainer, int index) {
		AppendTarget ret=new AppendTarget();
		ret.method=EAppendTargetMethod.QContainer;
		ret.domSelector=dynamicContainer.getId();
		ret.arg1 = ""+index;
		ret.arg2 = "";
		return ret;
	}
	public static AppendTarget QContainer(IQContainer dynamicContainer, String selector, int index) {
		AppendTarget ret=new AppendTarget();
		ret.method=EAppendTargetMethod.QContainer;
		ret.domSelector=dynamicContainer.getId();
		ret.arg1 = ""+index;
		ret.arg2 = selector;
		return ret;
	}
	/**
	 * Select the node by selector within container node of c and inster just after this node.
	 * @param table
	 * @param selector
	 * @return
	 */
	public static AppendTarget QContainer_selector_after(IQContainer c, String selector) {
		AppendTarget ret=new AppendTarget();
		ret.method=EAppendTargetMethod.QContainer_selector_after;
		ret.domSelector=c.getId();
		ret.arg1 = "";
		ret.arg2 = selector;
		return ret;
	}
	public static AppendTarget ReplaceWith(String domSelector) {
		AppendTarget ret=new AppendTarget();
		ret.method=EAppendTargetMethod.replaceWith;
		ret.domSelector=domSelector;
		return ret;
	}
	public static AppendTarget ReplaceContent(String domSelector) {
		AppendTarget ret=new AppendTarget();
		ret.method=EAppendTargetMethod.replaceContent;
		ret.domSelector=domSelector;
		return ret;
	}
	/** Configure that the newly created content is not XHTML but SVG. */
	public AppendTarget setSvg()
	{
		nameSpaceUri=namespaceUriSvg;
		rootObjectType="svg";
		return this;
	}
	public static AppendTarget ReplaceContent(QComponent c) {
		return ReplaceContent("#"+c.getId());
	}
}
