package hu.qgears.quickjs.qpage.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import hu.qgears.commons.EscapeString;
import hu.qgears.quickjs.qpage.QButton;
import hu.qgears.quickjs.qpage.QLabel;
import hu.qgears.quickjs.qpage.QPageContainer;
import hu.qgears.quickjs.qpage.QTextEditor;
import hu.qgears.quickjs.utils.AbstractQPage;
import hu.qgears.quickjs.utils.QTimerTask;

/**
 * A simple example of a QPage based web application. 
 */
public class QExample01 extends AbstractQPage
{
	private static final Timer timer=new Timer("QExample01 server push update timer", true);
	private SimpleDateFormat df=new SimpleDateFormat("yyyy. M dd. HH:mm:ss");
	private QButton dispose=new QButton();
	private QButton buttonClear=new QButton();
	private QTextEditor textEd=new QTextEditor();
	private QLabel l=new QLabel("mylabel");

	@Override
	protected void initQPage(final QPageContainer page) {
		// Create text editor object, initialize string content 
		
		textEd.text.setPropertyFromServer("Example text to edit");
		
		// Create clear text area button and add a listener that actually clears the text editor area.
		buttonClear.clicked.addListener(msg->textEd.text.setPropertyFromServer(""));
		
		// Create dispose button and add a listener that actually disposes this page.
		dispose.clicked.addListener(msg->page.dispose());
		
		// Create a feedback label that is updated whenever the editor text area is edited.
		l.innerhtml.setPropertyFromServer("initial value");
		textEd.text.clientChangedEvent.addListener(msg->l.innerhtml.setPropertyFromServer(EscapeString.escapeHtml(msg)));
		
		// Create a label that is updated by a server side thread periodically. (To demonstrate server push feature.)
		final QLabel counter=new QLabel(page, "counter");
		counter.innerhtml.setPropertyFromServer("");
		
		// Timer task that passes execution to the UI executor to update the label to current time
		// Events that are coming from non UI scope (other threads) have to be submitted to the UI thread as in the example.
		QTimerTask tt=new QTimerTask(()->counter.getPage().submitToUI(()->counter.innerhtml.setPropertyFromServer(df.format(new Date()))));
		timer.schedule(tt, 1000, 1000);
		
		// In case the page is disposed cancel the updater timer (by user leaves pagem timeout, HTTP session ends or explicite dispose)
		page.disposedEvent.addOnReadyHandler(p->{tt.cancel(); System.out.println("Page disposed.");});
	}
	/**
	 * See the rtemplate version of this file <a href="https://github.com/rizsi/quickjs/blob/master/quickjs-example/template/hu/qgears/quickjs/qpage/example/QExample01.java.rt#L58">here</a>
	 */
	@Override
	protected void writeBody() {
		write("<h1>QPage example page</h1>\n<a href=\"/\">Back to index</a><br/>\n\n<h2>Text editor with feedback</h2>\n\n<textarea id=\"");
		writeObject(textEd.getId());
		write("\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"");
		writeObject(buttonClear.getId());
		write("\">Clear textbox</button>\n<button id=\"");
		writeObject(dispose.getId());
		write("\">Dispose page</button>\n<br/>\n<div id=\"mylabel\">static content</div>\nThe time is updated using server push:\n<div id=\"counter\">static content</div>\n");
	}
}
