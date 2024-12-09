package hu.qgears.quickjs.qpage.example;

import hu.qgears.quickjs.qpage.AbstractQPage;

public class Index extends AbstractQPage
{
	@Override
	public void createBody() {
		write("<a href=\"/01\">Example 01</a><br/>\n<a href=\"/02\">Example 02</a><br/>\n<a href=\"/03\">Example 03</a><br/>\n<a href=\"/04\">Example 04 - dynamic divs</a><br/>\n<!-- <a href=\"/simple\">Simple example</a><br/>\n<a href=\"/upload/\">File upload</a> -->\n");
	}

	@Override
	public void initPage() {
	}
}
