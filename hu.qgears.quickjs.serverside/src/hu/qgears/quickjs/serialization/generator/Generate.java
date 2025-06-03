package hu.qgears.quickjs.serialization.generator;

import joptsimple.annot.AnnotatedClass;

public class Generate {
	public static void main(String[] args) throws Exception {
		CmdArgs cmargs=new CmdArgs();
		AnnotatedClass ac= new AnnotatedClass();
		ac.parseAnnotations(cmargs);
		ac.parseArgs(args);
		ac.print();
		ProcessInterface.process(cmargs);
	}
}
