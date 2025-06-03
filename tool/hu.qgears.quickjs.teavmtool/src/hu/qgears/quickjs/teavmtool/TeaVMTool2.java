package hu.qgears.quickjs.teavmtool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilFile;
import hu.qgears.tools.build.teavm.BundleAdditionalInfoTeaVm;
import joptsimple.tool.AbstractTool;

public class TeaVMTool2 extends AbstractTool {
	private TeaVmCompileArgsGradle cargs;
	public static URL[] gatherTargetUrls(TeaVmCompileArgs cargs, List<File> classesFolders) throws MalformedURLException
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
		for(File f: classesFolders)
		{
			urls.add(f.toURI().toURL());
		}
		return urls.toArray(new URL[] {});
	}
	public static int reflectiveExec(TeaVmCompileArgs cargs, List<File> classesFolders, BundleAdditionalInfoTeaVm tea, File outputFile) throws Exception
	{
		ClassLoader cl=buildClassLoader(cargs);
		Class<?> cla=cl.loadClass("hu.qgears.quickjs.teavmtool.RunTool");
		Object runTool=cla.getDeclaredConstructor().newInstance();
		Field f=cla.getField("classLoaderUrls");
		f.set(runTool, gatherTargetUrls(cargs, classesFolders));
		
		f=cla.getField("out");
		f.set(runTool, outputFile);
		
		System.out.println("Call tea compiler output file: "+outputFile.getAbsolutePath());
		
		System.out.println("Main class: "+tea.mainClass);
		f=cla.getField("mainClass");
		f.set(runTool, tea.mainClass);

		f=cla.getField("optimizationLevel");
		f.set(runTool, cargs.optimizationLevel);

		f=cla.getField("obfuscated");
		f.set(runTool, cargs.obfuscated);
		
		f=cla.getField("cacheFolder");
		f.set(runTool, cargs.cacheFolder);

		Method m=cla.getMethod("compile");
		try {
			return (Integer)m.invoke(runTool);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
	private static ClassLoader buildClassLoader(TeaVmCompileArgs cargs) throws IOException {
		ClassLoader rootClassloader=TeaVMTool2.class.getClassLoader();
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
				urls.add(f.toURI().toURL());
			}
		}
		
		File reflectiveLoadTrick=new File(cargs.getOutputFolder(), "reflectiveLoadTrick");

		Class<?>[]clas=RunTool.class.getNestMembers();
		for(Class<?> c: clas)
		{
			File outf=new File(reflectiveLoadTrick, c.getName().replaceAll("\\.", "\\/")+".class");
			outf.getParentFile().mkdirs();
			UtilFile.saveAsFile(outf, UtilFile.loadFile(TeaVMTool2.class.getResource(outf.getName())));
		}
		urls.add(reflectiveLoadTrick.toURI().toURL());
		
		URLClassLoader ret=new URLClassLoader(
				urls.toArray(new URL[] {}), rootClassloader);
		return ret;
	}

	@Override
	public String getId() {
		return "teavm-gradle-build";
	}

	@Override
	public String getDescription() {
		return "Compile TeaVM application from gradle output";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		cargs=(TeaVmCompileArgsGradle) a;
		int ret=0;
		long t0=System.currentTimeMillis();
		BundleAdditionalInfoTeaVm info=new BundleAdditionalInfoTeaVm(cargs.mainClass);
		File buildDir=cargs.out;
		List<File> classesToBuild=new ArrayList<File>();
		classesToBuild.add(new File(buildDir, "build/classes/java/main"));
		reflectiveExec(cargs, classesToBuild, info, new File(buildDir, "out.js"));
		long t1=System.currentTimeMillis();
		System.out.println("Spent millis: "+(t1-t0));
		return ret;
	}
	@Override
	protected IArgs createArgsObject() {
		return new TeaVmCompileArgsGradle();
	}
}
