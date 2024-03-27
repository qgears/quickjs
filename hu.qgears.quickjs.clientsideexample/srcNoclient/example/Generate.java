package example;

import hu.qgears.quickjs.serialization.ProcessInterface;

public class Generate {
	public static void main(String[] args) throws Exception {
		new ProcessInterface(ExampleRemoteIf.class).process(ExampleRemoteIf.class);
	}
}
