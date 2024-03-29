package example;

import hu.qgears.quickjs.serialization.generator.ProcessInterface;

public class Generate {
	public static void main(String[] args) throws Exception {
		new ProcessInterface(ExampleRemoteIf.class).process(ExampleRemoteIf.class);
	}
}
