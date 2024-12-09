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
import org.teavm.diagnostics.ProblemSeverity;
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
	public int compile() throws Exception {
		run();
		return 0;
	}
	public URL[] classLoaderUrls;
	public File out;
	public File cacheFolder;
	public String mainClass;
	
	public int optimizationLevel=0;
	public boolean obfuscated=false;
	
	
	private ClassLoader buildClassLoader() throws MalformedURLException {
		ClassLoader rootClassloader=getClass().getClassLoader();
		URLClassLoader ret=new URLClassLoader(
				classLoaderUrls, rootClassloader);
		return ret;
	}
	private void run() throws Exception {
		List<SourceFileProvider> sourceFileProviders = new ArrayList<>();
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
        tool.setOptimizationLevel(TeaVMOptimizationLevel.values()[optimizationLevel]);
        /// Results in more classes and bigger result js file.
        tool.setFastDependencyAnalysis(false);

        tool.setSourceMapsFileGenerated(false);
        tool.setDebugInformationGenerated(false);
        tool.setSourceFilesCopied(false);

        tool.setObfuscated(obfuscated);
        tool.setStrict(true);
        tool.setMaxTopLevelNames(1000);
        tool.getTransformers().addAll(Arrays.asList());
        tool.getClassesToPreserve().addAll(Arrays.asList());
        if(cacheFolder!=null)
        {
            /// Makes compilation about 20% faster 
            tool.setIncremental(true);
            tool.setCacheDirectory(cacheFolder);
            System.out.println("Use cache folder: "+cacheFolder.getAbsolutePath());
        }else
        {
            tool.setIncremental(false);
        }
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

        @SuppressWarnings("unused")
		var generatedFiles = tool.getGeneratedFiles().stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
        List<Problem> problems=tool.getProblemProvider().getProblems();
        boolean hasError=false;
        for(Problem p: problems)
        {
        	System.out.print(p.getSeverity()+" "+p.getText());
        	if(p.getSeverity()==ProblemSeverity.ERROR)
        	{
        		hasError=true;
        	}
        	for(Object o: p.getParams())
        	{
            	System.out.print(" "+o);
        	}
        	System.out.println();
        	CallLocation cl=p.getLocation();
        	if(cl!=null)
        	{
        		System.out.println("Source method: "+cl.getMethod());
        		System.out.println("Source sourceLocation: "+cl.getSourceLocation());
        	}else
        	{
        		System.out.println("getLocation() is null");
        	}
        }
        if(hasError)
        {
        	throw new RuntimeException("Compile error: see std output");
        }
	}
}
