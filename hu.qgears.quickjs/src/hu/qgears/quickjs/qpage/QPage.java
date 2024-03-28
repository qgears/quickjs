package hu.qgears.quickjs.qpage;

import java.io.IOException;

import org.json.JSONObject;

/** A page of an SPA application - the content of the <body>
 * Exactly single instance must exist all time below the QPageContainer object.
 **/
public class QPage extends QComponent {
	@Override
	protected void doInitJSObject() {
		write("\tnew QPage(page, \"");
		writeObject(id);
		write("\");\n");
	}
	@Override
	public void handle(JSONObject post) throws IOException {
		// Nothing to do
	}
}
