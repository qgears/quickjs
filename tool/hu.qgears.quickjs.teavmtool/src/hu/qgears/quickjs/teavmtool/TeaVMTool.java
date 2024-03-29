package hu.qgears.quickjs.teavmtool;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.tools.Tools;
import joptsimple.tool.AbstractTool;

public class TeaVMTool extends AbstractTool {
	public static void main(String[] args) throws Exception {
		Tools t=new Tools();
		t.register(new TeaVMTool());
		t.mainEntryPoint(args);
	}
	private URL[] gatherTargetUrls() throws MalformedURLException
	{
		List<URL> urls=new ArrayList<>();
		File g=new File("/home/rizsi/rizsi.com/jspa-binary-deps/tools/teavm");
		for(File f: g.listFiles())
		{
			if(f.getName().endsWith(".jar"))
			{
				// System.out.println(f.getAbsolutePath());
				urls.add(f.toURI().toURL());
			}
		}
//		urls.add(new File("/home/rizsi/Downloads/teavm-classlib-0.9.2.jar").toURI().toURL());
//		urls.add(new File("/home/rizsi/Downloads/teavm-platform-0.9.2.jar").toURI().toURL());
//		urls.add(new File("/home/rizsi/Downloads/teavm-jso-0.9.2.jar").toURI().toURL());
		
		//urls.add(new File("/home/rizsi/Downloads/teavm-jso-apis-0.9.2.jar").toURI().toURL());
		//urls.add(new File("/home/rizsi/Downloads/teavm-jso-impl-0.9.2.jar").toURI().toURL());
		//		new File("/home/rizsi/Downloads/teavm-interop-0.9.2.jar").toURI().toURL(),
//		new File("/home/rizsi/Downloads/teavm-core-0.9.2.jar").toURI().toURL(),
//		new File("/home/rizsi/Downloads/teavm-tooling-0.9.2.jar").toURI().toURL(),

		urls.add(new File("/home/rizsi/git-qgears/fos/quickjs/3rdparty/org.slf4j.api/bin").toURI().toURL());
		urls.add(new File("/home/rizsi/git-qgears/fos/quickjs-clientside/hu.qgears.quickjs/bin").toURI().toURL());
		urls.add(new File("/home/rizsi/git-qgears/fos/quickjs-clientside/hu.qgears.quickjs.clientsideexample/bin").toURI().toURL());
		urls.add(new File("/home/rizsi/openproto/openproto/commons/hu.qgears.commons/bin").toURI().toURL());
		return urls.toArray(new URL[] {});
	}
	private int reflectiveExec() throws Exception
	{
		ClassLoader cl=buildClassLoader();
		Class<?> cla=cl.loadClass("hu.qgears.quickjs.teavmtool.RunTool");
		Object runTool=cla.getDeclaredConstructor().newInstance();
		Field f=cla.getField("classLoaderUrls");
		f.set(runTool, gatherTargetUrls());
		
		f=cla.getField("out");
		f.set(runTool, new File("/home/rizsi/tmp/teavm/example.js"));
		
		f=cla.getField("mainClass");
		f.set(runTool, "example.MainClass");
		Method m=cla.getMethod("compile");
		try {
			return (Integer)m.invoke(runTool);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
	private static ClassLoader buildClassLoader() throws MalformedURLException {
		//ClassLoader rootClassloader=ClassLoader.getSystemClassLoader();
		//while(rootClassloader.getParent()!=null)
		//{
		//	rootClassloader=rootClassloader.getParent();
		//}
		ClassLoader rootClassloader=TeaVMTool.class.getClassLoader().getParent();
		List<URL> urls=new ArrayList<>();
		File g=new File("/home/rizsi/rizsi.com/jspa-binary-deps/tools/teavm");
		for(File f: g.listFiles())
		{
			if(f.getName().endsWith(".jar"))
			{
				// System.out.println(f.getAbsolutePath());
				urls.add(f.toURI().toURL());
			}
		}
		
		// urls.add(new File("/home/rizsi/git-qgears/fos/quickjs/tool/teavmtool0/bin").toURI().toURL());
		urls.add(new File("/home/rizsi/git-qgears/fos/quickjs/tool/hu.qgears.quickjs.teavmtool/bin").toURI().toURL());
		
//		urls.add(new File("/home/rizsi/Downloads/teavm-classlib-0.9.2.jar").toURI().toURL());
//		urls.add(new File("/home/rizsi/Downloads/teavm-platform-0.9.2.jar").toURI().toURL());
//		urls.add(new File("/home/rizsi/Downloads/teavm-jso-0.9.2.jar").toURI().toURL());
		
		//urls.add(new File("/home/rizsi/Downloads/teavm-jso-apis-0.9.2.jar").toURI().toURL());
		//urls.add(new File("/home/rizsi/Downloads/teavm-jso-impl-0.9.2.jar").toURI().toURL());
		//		new File("/home/rizsi/Downloads/teavm-interop-0.9.2.jar").toURI().toURL(),
//		new File("/home/rizsi/Downloads/teavm-core-0.9.2.jar").toURI().toURL(),
//		new File("/home/rizsi/Downloads/teavm-tooling-0.9.2.jar").toURI().toURL(),

//		urls.add(new File("/home/rizsi/git-qgears/fos/quickjs/3rdparty/org.slf4j.api/bin").toURI().toURL());
//		urls.add(new File("/home/rizsi/git-qgears/fos/quickjs-clientside/hu.qgears.quickjs/bin").toURI().toURL());
//		urls.add(new File("/home/rizsi/git-qgears/fos/quickjs-clientside/hu.qgears.quickjs.clientsideexample/bin").toURI().toURL());
//		urls.add(new File("/home/rizsi/openproto/openproto/commons/hu.qgears.commons/bin").toURI().toURL());
		URLClassLoader ret=new URLClassLoader(
				urls.toArray(new URL[] {}), rootClassloader);
		return ret;
	}

	@Override
	public String getId() {
		return "teavm-build";
	}

	@Override
	public String getDescription() {
		return "Compile TeaVM application to JS from osgi bundles";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		return reflectiveExec();
	}

	@Override
	protected IArgs createArgsObject() {
		return new TeaVmCompileArgs();
	}
}
