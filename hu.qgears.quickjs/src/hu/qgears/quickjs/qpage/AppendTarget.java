package hu.qgears.quickjs.qpage;

public class AppendTarget {
	public String domSelector;
	public EAppendTargetMethod method;
	public String arg1;
	public String arg2;
	public String nameSpaceUri=namespaceUriHtml;
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
	public static AppendTarget ReplaceWith(String domSelector) {
		AppendTarget ret=new AppendTarget();
		ret.method=EAppendTargetMethod.replaceWith;
		ret.domSelector=domSelector;
		return ret;
	}
	public AppendTarget setSvg()
	{
		nameSpaceUri=namespaceUriSvg;
		rootObjectType="svg";
		return this;
	}
}
