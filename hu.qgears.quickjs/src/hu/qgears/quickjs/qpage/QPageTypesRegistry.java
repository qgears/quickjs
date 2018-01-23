package hu.qgears.quickjs.qpage;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class QPageTypesRegistry {
	private Map<String, QComponent> types=new TreeMap<>();
	private static QPageTypesRegistry instance=new QPageTypesRegistry();
	public static QPageTypesRegistry getInstance() {
		return instance;
	}
	public QPageTypesRegistry() {
		registerType(new QButton(null, null));
		registerType(new QLabel(null, null));
		registerType(new QTextEditor(null, null));
		registerType(new QSelectCombo(null, null));
		registerType(new QSelectFastScroll(null, null));
		registerType(new QDiv(null, null));
		registerType(new QImage(null, null));
	}
	public void registerType(QComponent c)
	{
		types.put(c.getClass().getSimpleName(), c);
	}
	public Collection<QComponent> getTypes() {
		return types.values();
	}
	public QComponent getType(String substring) {
		return types.get(substring);
	}
}
