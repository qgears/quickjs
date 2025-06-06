package hu.qgears.quickjs.qpage;

import java.util.ArrayList;
import java.util.List;

public class QSelectFastScroll extends QSelect {
	static List<String> scriptReferences=new ArrayList<>();
	{
		scriptReferences.add("fastscroll.js");
		scriptReferences.add(QSelectFastScroll.class.getSimpleName()+".js");
	}
	public QSelectFastScroll(IQContainer parent, String id) {
		super(parent, id);
	}
	public QSelectFastScroll(IQContainer parent) {
		super(parent);
	}
	
//	@Override
//	public void generateHtmlObject() {
//		write("<div id=\"");
//		writeJSValue(id);
//		write("\" style=\"width:650px; height:150px;\"></div>\t\n");
//	}
	@Override
	public List<String> getScriptReferences() {
		return scriptReferences;
	}
	
	public void generateHeader(HtmlTemplate parent)
	{
		new HtmlTemplate(parent){

			public void generate() {
				try {
					write("<style>\n.option:hover {\n    background-color: yellow;\n}\n.option:active {\n    background-color: red;\n}\n</style>\n<script language=\"javascript\" type=\"text/javascript\">\n");
					writeObject(getPageContainer().getPlatform().loadResource("fastscroll.js"));
					write("\n");
					writeObject(getPageContainer().getPlatform().loadResource("QSelectFastScroll.js"));
					write("\n</script>\n");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
		}.generate();
	}
}
