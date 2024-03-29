package hu.qgears.quickjs.teavmtool;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.teavm.diagnostics.Problem;
import org.teavm.model.CallLocation;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.TeaVMToolLog;
import org.teavm.tooling.sources.SourceFileProvider;
import org.teavm.vm.TeaVMOptimizationLevel;
import org.teavm.vm.TeaVMPhase;
import org.teavm.vm.TeaVMProgressFeedback;
import org.teavm.vm.TeaVMProgressListener;

/**
 * Must be loaded into a classloader where only the TeaVM compiler is accessible
 * but nothing else is present because any class that is loadable by this classloader will override
 * the classes of the application to be loaded.
 */
public class RunTool {
	public static void main(String[] args) throws Exception {
		new RunTool().run();
	}
	public static int compile() throws Exception {
		new RunTool().run();
		return 0;
	}
	private ClassLoader buildClassLoader() throws MalformedURLException {
		//ClassLoader rootClassloader=ClassLoader.getSystemClassLoader();
		//while(rootClassloader.getParent()!=null)
		//{
		//	rootClassloader=rootClassloader.getParent();
		//}
		ClassLoader rootClassloader=getClass().getClassLoader();
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
		URLClassLoader ret=new URLClassLoader(
				urls.toArray(new URL[] {}), rootClassloader);
		return ret;
	}
	private void run() throws Exception {
		File out=new File("/home/rizsi/tmp/teavm/example.js");
		String mainClass="example.MainClass";
		List<SourceFileProvider> sourceFileProviders = new ArrayList<>();
//		sourceFileProviders.add(new DirectorySourceFileProvider(new File("/home/rizsi/git-qgears/fos/quickjs-clientside/hu.qgears.quickjs.clientsideexample/bin")));
		
		TeaVMTool tool=new TeaVMTool();
		TeaVMProgressListener pl=new TeaVMProgressListener() {
			
			@Override
			public TeaVMProgressFeedback progressReached(int arg0) {
				return TeaVMProgressFeedback.CONTINUE;
			}
			
			@Override
			public TeaVMProgressFeedback phaseStarted(TeaVMPhase arg0, int arg1) {
				return TeaVMProgressFeedback.CONTINUE;
			}
		};
        tool.setProgressListener(pl);
        tool.setLog(new TeaVMToolLog() {
			
			@Override
			public void warning(String arg0, Throwable arg1) {
				System.out.println("warning: "+arg0);
			}
			
			@Override
			public void warning(String arg0) {
				System.out.println("warning: "+arg0);
			}
			
			@Override
			public void info(String arg0, Throwable arg1) {
				System.out.println("info: "+arg0);
			}
			
			@Override
			public void info(String arg0) {
				System.out.println("info: "+arg0);
			}
			
			@Override
			public void error(String arg0, Throwable arg1) {
				System.out.println("ERROR: "+arg0);
			}
			
			@Override
			public void error(String arg0) {
				System.out.println("ERROR: "+arg0);
			}
			
			@Override
			public void debug(String arg0, Throwable arg1) {
				System.out.println("DEBUG: "+arg0);
			}
			
			@Override
			public void debug(String arg0) {
				System.out.println("DEBUG: "+arg0);
			}
		});
        tool.setTargetType(TeaVMTargetType.JAVASCRIPT);
        tool.setMainClass(mainClass);
        tool.setEntryPointName("main");
        tool.setTargetDirectory(out.getParentFile());
        tool.setTargetFileName(out.getName());
        var classLoader = buildClassLoader();
        tool.setClassLoader(classLoader);
        tool.setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE);
        tool.setFastDependencyAnalysis(false);

        tool.setSourceMapsFileGenerated(false);
        tool.setDebugInformationGenerated(false);
        tool.setSourceFilesCopied(false);

        tool.setObfuscated(false);
        tool.setStrict(true);
        tool.setMaxTopLevelNames(1000);
        tool.setIncremental(false);
        tool.getTransformers().addAll(Arrays.asList());
        tool.getClassesToPreserve().addAll(Arrays.asList());
        tool.setCacheDirectory(null);
        // tool.setWasmVersion();
        // tool.setMinHeapSize(minHeapSize);
        // tool.setMaxHeapSize(maxHeapSize);
        //tool.setHeapDump(heapDump);
        tool.setShortFileNames(true);
        tool.setAssertionsRemoved(false);

        // tool.getProperties().putAll(properties);

        for (SourceFileProvider fileProvider : sourceFileProviders) {
            tool.addSourceFileProvider(fileProvider);
        }

            tool.generate();

        var generatedFiles = tool.getGeneratedFiles().stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
        List<Problem> problems=tool.getProblemProvider().getProblems();
        for(Problem p: problems)
        {
        	System.out.print(p.getSeverity()+" "+p.getText());
        	for(Object o: p.getParams())
        	{
            	System.out.print(" "+o);
        	}
        	System.out.println();
        	CallLocation cl=p.getLocation();
        	System.out.println("Source: "+cl.getMethod());
        	System.out.println("Source: "+cl.getSourceLocation());
        }
	}
}
