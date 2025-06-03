package hu.qgears.quickjs.serialization.generator;

import java.io.File;
import java.util.List;

import joptsimple.annot.JOHelp;
import joptsimple.tool.AbstractTool.IArgs;

public class CmdArgs implements IArgs {
	public String packageName;
	public String classPrefix="R";
	public File outputFolder;
	public List<File> classPath;
	@JOHelp("Class name of root interface of communication")
	public String iface;
	@Override
	public void validate() {
		if(iface==null)
		{
			throw new IllegalArgumentException("iface");
		}
		if(outputFolder==null)
		{
			throw new IllegalArgumentException("outputFolder");
		}
		if(packageName==null)
		{
			throw new IllegalArgumentException("packageName");
		}
	}
}
