package hu.qgears.quickjs.qpage.jetty;

import hu.qgears.quickjs.qpage.IQDisposableContainer;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.serialization.RemotingServer;
import hu.qgears.quickjs.serialization.SerializeBase;
import hu.qgears.quickjs.serverside.QPageContextServerSide;
import hu.qgears.quickjs.utils.gdpr.GdprSession;

public interface IContextConfigurator {
	void configurePageContext(QPage page, QPageContextServerSide context, QueryWrapperJetty queryWrapper);
	SerializeBase createSerializator();
	RemotingServer createRemotingServer(IQDisposableContainer container, GdprSession session);
}
