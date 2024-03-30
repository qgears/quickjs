package hu.qgears.quickjs.example.gui;

import hu.qgears.quickjs.spa.QSpa;
import hu.qgears.quickjs.spa.RoutingEndpointQPage;

public class ExampleGui {
	public QSpa createApplication()
	{
		QSpa ret=new QSpa();
		ret.method("GET").handle(new RoutingEndpointQPage(()->new ExamplePage01()));
		return ret;
	}
}
