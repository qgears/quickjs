package hu.qgears.quickjs.teavmtool;

import java.io.File;

import joptsimple.annot.JOHelp;

public class TeaVmCompileArgsGradle extends TeaVmCompileArgs {
	@JOHelp("Build directory where the gradle file is generated")
	public File out;
	@JOHelp("Name of main class")
	public String mainClass;
	@Override
	public void validate() {
		if(out==null)
		{
			throw new IllegalArgumentException("out must not be null");
		}
		super.validate();
	}
	@Override
	public File getOutputFolder() {
		return out;
	}
}
