package hu.qgears.quickjs.qpage.example;

import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.qpage.QSelect;
import hu.qgears.quickjs.qpage.QSelectFastScroll;

/**
 * A simple example of a QPage based web application. 
 */
public class QExample03 extends QExample02
{

	@Override
	protected QSelect createQSelect(QPage page, String string) {
		return new QSelectFastScroll(page, string);
	}
	@Override
	protected String getTypeName()
	{
		return "QSelectFastScroll";
	}
	protected void writeSelectHtml(int i) {
		write("<div id=\"");
		writeObject(selarr[i].getId());
		write("\" style=\"width:650px; height:150px;\"></div>\n");
	}
}
