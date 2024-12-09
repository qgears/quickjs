package hu.qgears.quickjs.teavmtool;

import hu.qgears.quickjs.serialization.generator.CmdArgs;
import hu.qgears.quickjs.serialization.generator.ProcessInterface;
import joptsimple.annot.AnnotatedClass;
import joptsimple.tool.AbstractTool;

public class CommGenTool extends AbstractTool {
	@Override
	public String getId() {
		return "generate-comm";
	}
	@Override
	public String getDescription() {
		return "Generate (browser-server) communication API and implementation";
	}
	@Override
	protected int doExec(IArgs a) throws Exception {
		CmdArgs cargs=(CmdArgs) a;
		int ret=0;
		printValues(System.out);
		
		ret=ProcessInterface.process(cargs);
		return ret;
	}
	@Override
	protected IArgs createArgsObject() {
		return new CmdArgs();
	}
}
