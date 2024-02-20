package hu.qgears.quickjs.qpage;

import java.io.StringWriter;
import java.io.Writer;

/**
 * Create DOM by a server side HTML template and append that DOM to a parent node.
 */
public class ComponentCreator extends AbstractDOMCreator
{
	/**
	 * Create HTML of this component into the client side HTML tree.
	 * @param qComponent
	 */
	public void createHtmlFor(QComponent qComponent) {
		IQContainer c=qComponent.getParent();
		if(c instanceof QComponent)
		{
			QComponent parent=(QComponent) c;
			try(ResetOutputObject cl=setParent(qComponent))
			{
				StringWriter generatedHtml=new StringWriter();
				Writer prev=setWriter(generatedHtml);
				try
				{
					doGenerateHtml(qComponent);
				}finally
				{
					setWriter(prev);
				}
				int index=additionalObject(generatedHtml.toString());
				write("page.components['");
				writeJSValue(parent.getId());
				write("'].createHTMLInto");
				writeObject(createIndex==null?"":"Index");
				write("(");
				writeObject(createIndex==null?"":(""+createIndex+","));
				write("args[");
				writeObject(index);
				write("]);\n");
			}
		}else
		{
			throw new RuntimeException("Dynamic creation is only possible within a QComponent container. Parent is: "+c);
		}
	}
	/**
	 * Possible to override by subclasses to generate HTML directly instead of calling the
	 * owner element.
	 * @param qComponent
	 */
	protected void doGenerateHtml(QComponent qComponent) {
		qComponent.generateHtmlObject(this);
	}
}
