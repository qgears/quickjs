package hu.qgears.quickjs.qpage.example;

import hu.qgears.quickjs.utils.AbstractQPage;

public interface IQPageFactory {
	AbstractQPage createPage(Object userData) throws Exception;
}
