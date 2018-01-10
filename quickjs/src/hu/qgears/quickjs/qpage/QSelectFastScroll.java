package hu.qgears.quickjs.qpage;

import java.io.IOException;

import hu.qgears.commons.UtilFile;

public class QSelectFastScroll extends QSelect {

	public QSelectFastScroll(QPage page0, String id) {
		super(page0, id);
	}
	
	@Override
	public void generateExampleHtmlObject(HtmlTemplate parent) {
		setWriter(parent.getWriter());
		write("<div id=\"");
		writeJSValue(id);
		write("\" style=\"width:650px; height:150px;\"></div>\t\n");
		setWriter(null);
	}
	
	public static void generateHeader(HtmlTemplate parent)
	{
		new HtmlTemplate(parent){

			public void generate() {
				try {
					write("<style>\n.option:hover {\n    background-color: yellow;\n}\n.option:active {\n    background-color: red;\n}\n</style>\n<script language=\"javascript\" type=\"text/javascript\">\n");
					writeObject(UtilFile.loadAsString(getClass().getResource("fastscroll.js")));
					write("class QSelectFastScroll extends QComponent\n{\n\taddDomListeners()\n\t{\n\t\tthis.fs=new FastScroll(this.dom);\n//\t\tthis.dom.onchange=this.onchange.bind(this);\n\t\tthis.fs.setClickListener(this.onchange.bind(this));\n\t}\n\tonchange(index)\n\t{\n\t\tvar fd=this.page.createFormData(this);\n\t\tfd.append(\"selected\", index);\n\t\tthis.page.send(fd);\n\t}\n\tsetSelected(value)\n\t{\n\t\tthis.fs.select(value);\n\t}\n\tsetOptions(options)\n\t{\n\t\tthis.fs.setEntries(options);\n\t}\n}\n</script>\n");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		}.generate();
	}

}
