package hu.qgears.quickjs.qpage;

public class HistoryPopStateEvent {
	public final QPage page;
	public final String pathname;
	public final String search;
	public HistoryPopStateEvent(QPage qPage, String pathname, String search) {
		this.page=qPage;
		this.pathname=pathname;
		this.search=search;
	}
}
