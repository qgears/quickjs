package hu.qgears.quickjs.qpage.example;

import hu.qgears.quickjs.qpage.QPage;

public class Index extends AbstractQPage
{

	@Override
	protected void initQPage(QPage page) {
	}

	@Override
	protected void writeBody() {
		write("<a href=\"/01\">Example 01</a><br/>\n<a href=\"/02\">Example 02</a><br/>\n<a href=\"/03\">Example 03</a><br/>\n");
	}

}
