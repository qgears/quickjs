package hu.qgears.quickjs.teavmtool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import joptsimple.annot.JOHelp;
import joptsimple.tool.AbstractTool.IArgs;

abstract public class TeaVmCompileArgs implements IArgs {
	@JOHelp("List of bundles to build. If not specified then all are built.")
	public List<String> bundlesToBuild=new ArrayList<>();
	@JOHelp("Folder that contains the TeaVM tooling with all of its dependencies. Added to classpath when loading the compiler.")
	public File teavmJarsFolder;
	@JOHelp("0 is lowest, 2 is highest optimization level")
	public int optimizationLevel=2;
	@JOHelp("Is output required to be obfuscated")
	public boolean obfuscated=true;
	@JOHelp("Cache folder used for incremental build")
	public File cacheFolder=new File("/tmp/teavmCacheFolder");
	@Override
	public void validate() {
	}
	abstract public File getOutputFolder();
}
