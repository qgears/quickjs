package hu.qgears.quickjs.qpage.jetty;

import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.serverside.QPageContextServerSide;

public interface IContextConfigurator {

	void configurePageContext(QPage page, QPageContextServerSide context, QueryWrapperJetty queryWrapper);

}
