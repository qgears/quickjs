package hu.qgears.quickjs.teavmtool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.tools.build.Args;
import joptsimple.annot.JOHelp;

public class TeaVmCompileArgs extends Args {
	@JOHelp("List of bundles to build. If not specified then all are built.")
	public List<String> bundlesToBuild=new ArrayList<>();
	@JOHelp("Folder that contains the TeaVM tooling with all of its dependencies. Added to classpath when loading the compiler.")
	public File teavmJarsFolder=new File("/home/rizsi/rizsi.com/jspa-binary-deps/tools/teavm");
	@Override
	public void validate() {
	}
}
