package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import hu.qgears.commons.UtilFile;
import hu.qgears.quickjs.qpage.QComponent;
import hu.qgears.quickjs.qpage.QPage;
import hu.qgears.quickjs.qpage.QPageTypesRegistry;

public class QPageJSHandler extends JSHandler
{

	@Override
	protected void writeTo(OutputStream os, String pathinfo) throws IOException {
		for(String s: QPage.scripts)
		{
			if(pathinfo.equals("/"+s))
			{
				os.write(UtilFile.loadFile(QPage.class.getResource(s)));
				return;
			}
		}
		{
			String name=pathinfo.substring(1, pathinfo.length()-3);
			if(name.contains("/")||name.contains(".."))
			{
				throw new IOException();
			}
			for(QComponent c: QPageTypesRegistry.getInstance().getTypes())
			{
				byte[] content=c.loadJs(name);
				if(content!=null)
				{
					os.write(content);
					return;
				}
			}
		}
	}
	@Override
	protected boolean handlesJs(String pathinfo) {
		for(String s: QPage.scripts)
		{
			if(pathinfo.equals("/"+s))
			{
				return true;
			}
		}
		QComponent t=QPageTypesRegistry.getInstance().getType(pathinfo.substring(1, pathinfo.length()-3));
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
