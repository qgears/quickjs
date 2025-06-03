package hu.qgears.quickjs.teavmtool;

import java.io.File;

import hu.qgears.tools.build.Args;
import joptsimple.annot.JODelegate;

public class TeaVMCompileArgsBundle extends TeaVmCompileArgs {
	@JODelegate(prefix = "")
	public Args bundleArgs = new Args();
	@Override
	public void validate() {
		super.validate();
		bundleArgs.validate();
	}
	@Override
	public File getOutputFolder() {
		return bundleArgs.out;
	}
}
