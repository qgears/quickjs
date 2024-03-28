package hu.qgears.quickjs.qpage.example;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.qpage.AppendTarget;
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
	int index=0;
	@Override
	protected void initQPage(QPage page) {
		dynamicContainer=new QDiv(page, "dynamicContainer");
		new QButton(page, "create").clicked.addListener(e->{
			System.out.println("Create pressed!");
			String childId="child"+index++;
			try(NoExceptionAutoClosable c=activateCreateDom(AppendTarget.QContainer(dynamicContainer)))
			{
				QDiv child=new QDiv(dynamicContainer, childId);
				QButton buttonDelete=new QButton(child);
				buttonDelete.clicked.addListener(ev->{System.out.println("delete clicked!");
					child.dispose();
				});
				write("<div id=\"");
				writeValue(child.getId());
				write("\">Cicuka ");
				writeObject(childId);
				write("<button id=\"");
				writeValue(buttonDelete.getId());
				write("\">Delete this node</button></div>\n");
			};
		});
	}

	@Override
	protected void writeBody() {
		write("<h1>Dynamic container example</h1>\n<a href=\"/\">Back to index</a><br/>\n<button id=\"create\">Create new dynamic content</button>\n<hr/>\n<div id=\"dynamicContainer\"></div>\n");
	}
}
