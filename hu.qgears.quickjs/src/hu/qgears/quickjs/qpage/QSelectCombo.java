package hu.qgears.quickjs.qpage;

public class QSelectCombo extends QSelect {

	public QSelectCombo(QPage page0, String id) {
		super(page0, id);
	}
	
	@Override
	public void generateExampleHtmlObject(HtmlTemplate parent) {
		setWriter(parent.getWriter());
		write("<select id=\"");
		writeJSValue(id);
		write("\" style=\"width: 250px;\" size=10></select>\t\n");
		setWriter(null);
	}

	
	public static void generateHeader(HtmlTemplate parent)
	{
		new HtmlTemplate(parent){

			public void generate() {
				write("<script language=\"javascript\" type=\"text/javascript\">\nclass QSelectCombo extends QComponent\n{\n\taddDomListeners()\n\t{\n\t\tthis.dom.onchange=this.onchange.bind(this);\n\t}\n\tonchange()\n\t{\n\t\tvar value=this.dom.options [this.dom.selectedIndex].value;\n\t\tvar fd=this.page.createFormData(this);\n\t\tfd.append(\"selected\", value);\n\t\tthis.page.send(fd);\n\t}\n\tsetSelected(value)\n\t{\n\t\tthis.dom.value=value;\n\t}\n\tsetOptions(options)\n\t{\n\t\twhile (this.dom.firstChild) {\n\t\t    this.dom.removeChild(this.dom.firstChild);\n\t\t}\n\t\tvar ctr=0;\n\t\tfor (var i in options){\n\t    \tvar opt = document.createElement('option');\n\t    \topt.value = ctr;\n\t    \topt.innerHTML = options[i];\n\t    \tctr++;\n//\t    \tif(ctr<500)\n\t    \t{\n\t\t    \tthis.dom.appendChild(opt);\n\t    \t}\n\t\t}\n\t}\n}\n</script>\n");
			}
			
		}.generate();
	}

}
