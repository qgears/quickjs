package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import hu.qgears.commons.UtilFile;
import hu.qgears.quickjs.serverside.QPageTypesRegistry;

public class QPageJSHandler extends JSHandler
{
	@Override
	protected void writeTo(OutputStream os, String pathinfo) throws IOException {
		URL t=QPageTypesRegistry.getInstance().getJsResources().get(pathinfo.substring(1));
		os.write(UtilFile.loadFile(t));
		return;
	}
	@Override
	protected boolean handlesJs(String pathinfo) {
		URL t=QPageTypesRegistry.getInstance().getJsResources().get(pathinfo.substring(1));
		if(t!=null)
		{
			return true;
		}
		return true;
	}
	@Override
	protected URL findResource(String pathinfo) {
		return null;
	}

}
