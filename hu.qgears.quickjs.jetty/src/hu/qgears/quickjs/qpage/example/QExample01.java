package hu.qgears.quickjs.qpage.example;

import java.text.SimpleDateFormat;
import java.util.Date;

import hu.qgears.commons.EscapeString;
import hu.qgears.quickjs.qpage.AbstractQPage;
import hu.qgears.quickjs.qpage.QButton;
import hu.qgears.quickjs.qpage.QLabel;
import hu.qgears.quickjs.qpage.QTextEditor;

/**
 * A simple example of a QPage based web application. 
 */
public class QExample01 extends AbstractQPage
{
	private SimpleDateFormat df=new SimpleDateFormat("yyyy. M dd. HH:mm:ss");
	private QButton dispose=new QButton();
	private QButton buttonClear=new QButton();
	private QTextEditor textEd=new QTextEditor();
	private QLabel l=new QLabel("mylabel");
	@Override
	public void initPage() {
	}
	@Override
	public void createBody() {
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
		
		// Timer task that passes execution to the UI executor to update the label to current time.
		// The timer is stored on the page and auto-closed when the page is closed. The returned object can also be used to close it.
		page.startTimer(()->counter.innerhtml.setPropertyFromServer(df.format(new Date())), 1000, 1000);
		
		// In case the page is disposed cancel the updater timer (by user leaves page timeout, HTTP session ends or explicite dispose)
		page.addCloseable(()->{System.out.println("Page disposed.");});
		write("<h1>QPage example page</h1>\n<a href=\"/\">Back to index</a><br/>\n\n<h2>Text editor with feedback</h2>\n\n<textarea id=\"");
		writeObject(textEd.getId());
		write("\" rows=\"5\" cols=\"150\"></textarea>\n<br/>\n<button id=\"");
		writeObject(buttonClear.getId());
		write("\">Clear textbox</button>\n<button id=\"");
		writeObject(dispose.getId());
		write("\">Dispose page</button>\n<br/>\n<div id=\"mylabel\">static content</div>\nThe time is updated using server push:\n<div id=\"counter\">static content</div>\n");
	}
}
