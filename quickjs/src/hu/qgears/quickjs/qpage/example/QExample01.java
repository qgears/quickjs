package hu.qgears.quickjs.qpage.example;

import org.apache.commons.lang.StringEscapeUtils;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.quickjs.qpage.QButton;
import hu.qgears.quickjs.qpage.QLabel;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.qpage.QTextEditor;

/**
 * A simple example of a QPage based web application. 
 */
public class QExample01 extends AbstractQPage
{
	@Override
	protected void initQPage(final QPage page) {
		final QTextEditor accessRules=new QTextEditor(page, "texted");
		accessRules.text.setPropertyFromServer("Example text to edit");
		QButton buttonAccess=new QButton(page, "submit");
		buttonAccess.clicked.addListener(new UtilEventListener<QButton>() {
			@Override
			public void eventHappened(QButton msg) {
				accessRules.text.setPropertyFromServer("");
			}
		});
		new QButton(page, "dispose").clicked.addListener(new UtilEventListener<QButton>() {
			public void eventHappened(QButton msg) {
				page.dispose();
			};
		});
		final QLabel l=new QLabel(page, "mylabel");
		accessRules.text.clientChangedEvent.addListener(new UtilEventListener<String>() {
			public void eventHappened(String msg) {
				l.innerhtml.setPropertyFromServer(StringEscapeUtils.escapeHtml(msg));
			};
		});
		
		final QLabel counter=new QLabel(page, "counter");
		new Thread("QExample Counter")
		{
			public void run() {
				while(!page.disposedEvent.isDone())
				{
					counter.getPage().submitToUI(new Runnable() {
						
						@Override
						public void run() {
							counter.innerhtml.setPropertyFromServer(""+System.currentTimeMillis());
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				System.out.println("Page disposed, update thread finished.");
			};
		}
		.start();
		l.innerhtml.setPropertyFromServer("initial value");;
	}
	@Override
	protected void writeBody() {
		write("<h1>QPage example page</h1>\n<a href=\"/\">Back to index</a><br/>\n\n<h2>Text editor with feedback</h2>\n\n<textarea id=\"texted\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"submit\">Clear textbox</button>\n<button id=\"dispose\">Dispose page</button>\n<br/>\n<div id=\"mylabel\">static content</div>\n<div id=\"counter\">static content</div>\n");
	}
}
