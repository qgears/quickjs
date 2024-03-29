package hu.qgears.quickjs.qpage;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class QPageTypesRegistry {
	private Map<String, QComponent> types=new TreeMap<>();
	private Map<String, URL> jsResources=new HashMap<>();
	private static QPageTypesRegistry instance=new QPageTypesRegistry();
	public static QPageTypesRegistry getInstance() {
		return instance;
	}
	public QPageTypesRegistry() {
		jsResources.put("indexedComm.js", QPage.class.getResource("indexedComm.js"));
		jsResources.put("QPage.js", QPage.class.getResource("QPage.js"));
		jsResources.put("QComponent.js", QPage.class.getResource("QComponent.js"));
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
		synchronized (jsResources) {
			c.registerResources(jsResources);
		}
	}
	public Collection<QComponent> getTypes() {
		return types.values();
	}
	public Set<String> getAllJsNames() {
		return new HashSet<>(jsResources.keySet());
	}
	public QComponent getType(String substring) {
		return types.get(substring);
	}
	public URL getResource(String resname) {
		synchronized (jsResources) {
			return jsResources.get(resname);
		}
	}
}
