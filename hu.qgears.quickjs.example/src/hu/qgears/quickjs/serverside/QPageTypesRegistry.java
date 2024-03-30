package hu.qgears.quickjs.serverside;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import hu.qgears.quickjs.qpage.QButton;
import hu.qgears.quickjs.qpage.QCheckbox;
import hu.qgears.quickjs.qpage.QComponent;
import hu.qgears.quickjs.qpage.QDateEditor;
import hu.qgears.quickjs.qpage.QDiv;
import hu.qgears.quickjs.qpage.QFileUpload;
import hu.qgears.quickjs.qpage.QImage;
import hu.qgears.quickjs.qpage.QLabel;
import hu.qgears.quickjs.qpage.QLink;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.qpage.QPageContainer;
import hu.qgears.quickjs.qpage.QRange;
import hu.qgears.quickjs.qpage.QSelectCombo;
import hu.qgears.quickjs.qpage.QSelectCombo2;
import hu.qgears.quickjs.qpage.QSelectFastScroll;
import hu.qgears.quickjs.qpage.QSvgContainer;
import hu.qgears.quickjs.qpage.QTextEditor;
import hu.qgears.quickjs.qpage.QTimeEditor;

public class QPageTypesRegistry {
	private Map<String, QComponent> types=new TreeMap<>();
	private Map<String, URL> jsResources=new HashMap<>();
	private List<String> jsOrder=new ArrayList<>();
	private static QPageTypesRegistry instance=new QPageTypesRegistry();
	public static QPageTypesRegistry getInstance() {
		return instance;
	}
	public static List<String> scripts=Arrays.asList("indexedComm.js", 
			"QPage.js", "QComponent.js");

	public QPageTypesRegistry() {
		jsResources.put("indexedComm.js", QPageContainer.class.getResource("indexedComm.js"));
		jsOrder.add("indexedComm.js");
		jsResources.put("QPageContainer.js", QPageContainer.class.getResource("QPageContainer.js"));
		jsOrder.add("QPageContainer.js");
		jsResources.put("QComponent.js", QPageContainer.class.getResource("QComponent.js"));
		jsOrder.add("QComponent.js");
		registerType(new QPage(null));
		registerType(new QButton(null, null));
		registerType(new QLabel(null, null));
		registerType(new QTextEditor(null, null));
		registerType(new QDateEditor(null, null));
		registerType(new QTimeEditor(null, null));
		registerType(new QCheckbox(null, null));
		registerType(new QSelectCombo(null, null));
		registerType(new QSelectCombo2(null, null));
		registerType(new QSelectFastScroll(null, null));
		registerType(new QDiv(null, null));
		registerType(new QImage(null, null));
		registerType(new QLink(null, null, null));
		registerType(new QFileUpload(null, null));
		registerType(new QSvgContainer(null, null));
		registerType(new QRange(null, null));
	}
	public void registerType(QComponent c)
	{
		types.put(c.getClass().getSimpleName(), c);
		for (String s: c.getScriptReferences())
		{
			addJs(s, c.getClass().getResource(s));
		}
	}
	public Collection<QComponent> getTypes() {
		return types.values();
	}
	public QComponent getType(String substring) {
		return types.get(substring);
	}
	public Map<String, URL> getJsResources()
	{
		return jsResources;
	}
	public URL getResource(String resname) {
		synchronized (jsResources) {
			URL ret=jsResources.get(resname);
			return ret;
		}
	}
	public void addJs(String jsFile, URL resource) {
		synchronized (jsResources) {
			jsResources.put(jsFile, resource);
			jsOrder.add(jsFile);
		}
	}
	public List<String> getJsOrder() {
		return jsOrder;
	}
	public Set<String> getAllJsNames() {
		return new HashSet<>(jsResources.keySet());
	}
}
