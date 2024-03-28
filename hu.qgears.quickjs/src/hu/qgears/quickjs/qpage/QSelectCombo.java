package hu.qgears.quickjs.qpage;

public class QSelectCombo extends QSelect {

	public QSelectCombo(IQContainer parent, String id) {
		super(parent, id);
	}
	public QSelectCombo(IQContainer parent) {
		super(parent);
	}	
//	@Override
//	public void generateHtmlObject() {
//		write("<select id=\"");
//		writeJSValue(id);
//		write("\" style=\"width: 250px;\" size=10></select>\t\n");
//	}
	public String getSelectionValue() {
		Integer sel=selected.getProperty();
		if(sel!=null && sel>=0 && sel<options.getProperty().size())
		{
			return options.getProperty().get(sel);
		}
		return null;
	}
}
