package hu.qgears.quickjs.qpage.example;

import hu.qgears.quickjs.qpage.QButton;
import hu.qgears.quickjs.qpage.QDiv;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.utils.AbstractQPage;

/**
 * A simple example of a QPage based web application. 
 */
public class QExample04DynamicDivs extends AbstractQPage
{
	@Override
	protected void writeHeaders() {
		write("<title>Dynamic div creation and delete example</title>\n");
		super.writeHeaders();
	}
	private QDiv dynamicContainer;
	private int index;
	@Override
	protected void initQPage(QPage page) {
		dynamicContainer=new QDiv(page, "dynamicContainer");
		new QButton(page, "create").clicked.addListener(e->{
			System.out.println("Pressed!");
			String childId="Dynamic-"+index++;
			QDiv child=new QDiv(dynamicContainer, childId)
			{
				@Override
				public void generateHtmlObject() {
					write("<div id=\"");
					writeValue(childId);
					write("\">Cicuka ");
					writeObject(childId);
					write("<button id=\"");
					writeValue(childId+".delete");
					write("\">Delete this node</button></div>\n");
				}
			};
			QButton buttonDelete=new QButton(child, childId+".delete");
			buttonDelete.clicked.addListener(ev->{System.out.println("delete clicked!");
				child.dispose();
			});
			child.setCreator(page.defaultCreator);
		});
	}

	@Override
	protected void writeBody() {
		write("<h1>Dynamic container example</h1>\n<a href=\"/\">Back to index</a><br/>\n<button id=\"create\">Create new dynamic content</button>\n<hr/>\n<div id=\"dynamicContainer\"></div>\n");
	}
}
