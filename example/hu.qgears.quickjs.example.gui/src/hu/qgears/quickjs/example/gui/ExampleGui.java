package hu.qgears.quickjs.example.gui;

import hu.qgears.quickjs.spa.QSpa;

public class ExampleGui {
	public QSpa createApplication()
	{
		QSpa ret=new QSpa().on("/", ()->new ExamplePage01());
		return ret;
	}
}
