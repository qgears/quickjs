package hu.qgears.quickjs.teavmtool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import hu.qgears.commons.UtilFile;
import hu.qgears.tools.Tools;
import hu.qgears.tools.build.BundleManifest;
import hu.qgears.tools.build.BundleResolver;
import hu.qgears.tools.build.BundleSet;
import hu.qgears.tools.build.Resolver;
import hu.qgears.tools.build.gen.BuildGenContext;
import hu.qgears.tools.build.teavm.BundleAdditionalInfoTeaVm;
import joptsimple.tool.AbstractTool;

public class TeaVMTool extends AbstractTool {
	private TeaVmCompileArgs cargs;
	public static void main(String[] args) throws Exception {
		Tools t=new Tools();
		t.register(new TeaVMTool());
		t.mainEntryPoint(args);
	}
	private URL[] gatherTargetUrls(BundleSet toCompile) throws MalformedURLException
	{
		List<URL> urls=new ArrayList<>();
		File g=cargs.teavmJarsFolder;
		for(File f: g.listFiles())
		{
			if(f.getName().endsWith(".jar"))
			{
				urls.add(f.toURI().toURL());
			}
		}
		for(BundleManifest bm: toCompile.all)
		{
			switch (bm.type)
			{
			case source:
			{
				for(String s: bm.cph.outputs)
				{
					File f=new File(bm.projectFile, s);
					urls.add(f.toURI().toURL());
				}
				break;
			}
			case binary:
			{
				urls.add(bm.projectFile.toURI().toURL());
				break;
			}
			case dummy:
			default:
				throw new RuntimeException("type unknown: "+bm.type);
			}
		}
		return urls.toArray(new URL[] {});
	}
	private int reflectiveExec(BundleSet toCompile, BundleManifest b) throws Exception
	{
		BundleAdditionalInfoTeaVm tea=BundleAdditionalInfoTeaVm.get(b);
		ClassLoader cl=buildClassLoader();
		Class<?> cla=cl.loadClass("hu.qgears.quickjs.teavmtool.RunTool");
		Object runTool=cla.getDeclaredConstructor().newInstance();
		Field f=cla.getField("classLoaderUrls");
		f.set(runTool, gatherTargetUrls(toCompile));
		
		if(tea.outputJsPath==null)
		{
			throw new IllegalArgumentException("outputJsPath not specified for bundle: "+b.id+" (in teavm.properties file)");
		}
		File outputFile=new File(b.projectFile, tea.outputJsPath);
		f=cla.getField("out");
		f.set(runTool, outputFile);
		
		System.out.println("Call tea compiler output file: "+outputFile.getAbsolutePath());
		
		System.out.println("Main class: "+tea.mainClass);
		f=cla.getField("mainClass");
		f.set(runTool, tea.mainClass);

		f=cla.getField("optimizationLevel");
		f.set(runTool, 0);

		f=cla.getField("obfuscated");
		f.set(runTool, false);

		Method m=cla.getMethod("compile");
		try {
			return (Integer)m.invoke(runTool);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
	private ClassLoader buildClassLoader() throws IOException {
		ClassLoader rootClassloader=getClass().getClassLoader();
		while(rootClassloader.getParent()!=null)
		{
			rootClassloader=rootClassloader.getParent();
		}
		List<URL> urls=new ArrayList<>();
		File g=cargs.teavmJarsFolder;
		for(File f: g.listFiles())
		{
			if(f.getName().endsWith(".jar"))
			{
				// System.out.println(f.getAbsolutePath());
				urls.add(f.toURI().toURL());
			}
		}
		
		File reflectiveLoadTrick=new File(cargs.out, "reflectiveLoadTrick");

		Class<?>[]clas=RunTool.class.getNestMembers();
		for(Class<?> c: clas)
		{
			File outf=new File(reflectiveLoadTrick, c.getName().replaceAll("\\.", "\\/")+".class");
			outf.getParentFile().mkdirs();
			UtilFile.saveAsFile(outf, UtilFile.loadFile(getClass().getResource(outf.getName())));
		}
		// urls.add(new File("/home/rizsi/git-qgears/fos/quickjs/tool/teavmtool0/bin").toURI().toURL());
		urls.add(reflectiveLoadTrick.toURI().toURL());
		
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
		cargs=(TeaVmCompileArgs) a;
		BundleResolver br=new BundleResolver(cargs);
		br.addAdditionalInfoParser(BundleAdditionalInfoTeaVm.createParser());
		br.setBundleToBuildFilter(bmf->{return BundleAdditionalInfoTeaVm.get(bmf)!=null;});
		br.execute();
		int ret=0;
		try(BuildGenContext bgc=new BuildGenContext(cargs.out, cargs))
		{
			bgc.r=br.getResolver();
			if(cargs.bundlesToBuild.size()>0)
			{
				for(String s: cargs.bundlesToBuild)
				{
					HashSet<BundleManifest> bundles=bgc.r.allTobuildBundles.byId.get(s);
					if(bundles.size()!=1)
					{
						System.err.println();
						throw new RuntimeException("TeaVM Bundle found by id: "+s+" "+bundles.size()+" must be exactly 1");
					}
					BundleManifest b=bundles.iterator().next();
					buildBundle(bgc, b);
				}
			}else
			{
				for(BundleManifest b: bgc.r.allTobuildBundles.all)
				{
					System.out.println("Build bundle: "+b);
					buildBundle(bgc, b);
					// b.
	/*				BundleAdditionalInfoTeaVm tea=BundleAdditionalInfoTeaVm.get(b);
					if(tea!=null)
					{
						b.
					}*/
				}
			}
		}
		return ret;
	}

	private void buildBundle(BuildGenContext bgc, BundleManifest b) throws Exception {
		BundleSet bs=new BundleSet();
		BundleSet resultBundles=new BundleSet();
		bs.add(b);
		Resolver e=new Resolver(bs, bgc.r.allAvailableBundles, resultBundles);
		e.resolve();
		// System.out.println("BundleSet: "+e.state.resultBundles);
//		BundleAdditionalInfoTeaVm tea=BundleAdditionalInfoTeaVm.get(b);
//		String gradle=new GradleTemplate(e).setMainClass(tea.mainClass).generate();
//		File f=new File(cargs.out, b.id+"/build.gradle");
//		f.getParentFile().mkdirs();
//		UtilFile.saveAsFile(f, gradle);
		System.out.println("Call tea compiler on: "+b.id+" with "+e.state.resultBundles);
		reflectiveExec(e.state.resultBundles, b);
	}
	@Override
	protected IArgs createArgsObject() {
		return new TeaVmCompileArgs();
	}
}
