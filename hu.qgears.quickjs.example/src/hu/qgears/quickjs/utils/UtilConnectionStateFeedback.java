package hu.qgears.quickjs.utils;

import hu.qgears.quickjs.qpage.HtmlTemplate;

/**
 * Connection state feedback for the application
 */
public class UtilConnectionStateFeedback {
	/**
	 * Include JavaScript when all DOM nodes are created and globalQPage object exists.
	 * @param parent
	 */
	public static void jsAfterInit(HtmlTemplate parent)
	{
		new HtmlTemplate(parent)
		{
			public void generate()
			{
				write("    globalWarningVisible=false;\n    globalQPage.setShowStateCallback(state => {\n\t\tswitch(state)\n\t\t{\n\t\t\tcase 1:\n\t\t\t\tglobalWarningWisible=false;\n\t\t\t\tdocument.getElementById('alert-offline').classList.add(\"d-none\");\n\t\t\t\tdocument.getElementById('alert-disconnected').classList.add(\"d-none\");\n\t\t\t\tbreak;\n\t\t\tcase 0:\n\t\t\tcase 2:\n\t\t\t\tglobalWarningVisible=true;\n\t\t\t\tsetTimeout(function(){\n\t\t\t\t\tif(globalWarningVisible)\n\t\t\t\t\t{\n\t\t\t\t\t\tdocument.getElementById('alert-disconnected').classList.remove(\"d-none\");\n\t\t\t\t\t}\n\t\t\t\t}, 3000);\n\t\t\t\tdocument.getElementById('alert-offline').classList.add(\"d-none\");\n\t\t\tbreak;\n\t\t\tcase 3:\n\t\t\t\tdocument.getElementById('alert-disconnected').classList.add(\"d-none\");\n\t\t\t\tglobalWarningWisible=false;\n\t\t\t\t// Once in error state we do not go back -> we don't need a variable to protect the timeout\n\t\t\t\tsetTimeout(function(){\n\t\t\t\t\tdocument.getElementById('alert-offline').classList.remove(\"d-none\");\n\t\t\t\t}, 3000);\n\t\t\tbreak;\n\t\t}\n    });\n");
			}
		}.generate();
	}

	public static void domObjects(HtmlTemplate parent) {
		new HtmlTemplate(parent)
		{
			public void generate()
			{
				write("<div id=\"alert-offline\" class=\"alert alert-danger fixed-top d-none\" role=\"alert\">\n  Connection to server is closed. Try reloading the page!\n</div>\n<div id=\"alert-disconnected\" class=\"alert alert-danger fixed-top d-none\" role=\"alert\">\n  Connection to server is temporarily down. Wait for automatic fix or try reloading the page!\n</div>\n");
			}
		}.generate();
	}
}
