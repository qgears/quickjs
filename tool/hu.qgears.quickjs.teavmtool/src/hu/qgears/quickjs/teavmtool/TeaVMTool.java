package hu.qgears.quickjs.teavmtool;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import hu.qgears.tools.Tools;
import hu.qgears.tools.build.BundleManifest;
import hu.qgears.tools.build.BundleResolver;
import hu.qgears.tools.build.BundleSet;
import hu.qgears.tools.build.Resolver;
import hu.qgears.tools.build.gen.BuildGenContext;
import hu.qgears.tools.build.teavm.BundleAdditionalInfoTeaVm;
import joptsimple.tool.AbstractTool;

public class TeaVMTool extends AbstractTool {
	private TeaVMCompileArgsBundle cargs;
	public static void main(String[] args) throws Exception {
		Tools t=new Tools();
		t.register(new TeaVMTool());
		t.register(new TeaVMTool2());
		t.register(new CommGenTool());
		t.mainEntryPoint(args);
	}
	private List<File> gatherTargetFiles(BundleSet toCompile) throws MalformedURLException
	{
		List<File> classesFolders=new ArrayList<>();
		for(BundleManifest bm: toCompile.all)
		{
			switch (bm.type)
			{
			case source:
			{
				for(String s: bm.cph.outputs)
				{
					File f=new File(bm.projectFile, s);
					classesFolders.add(f);
				}
				break;
			}
			case binary:
			{
				classesFolders.add(bm.projectFile);
				break;
			}
			case dummy:
			default:
				throw new RuntimeException("type unknown: "+bm.type);
			}
		}
		return classesFolders;
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
		cargs=(TeaVMCompileArgsBundle) a;
		BundleResolver br=new BundleResolver(cargs.bundleArgs);
		br.addAdditionalInfoParser(BundleAdditionalInfoTeaVm.createParser());
		br.setBundleToBuildFilter(bmf->{return BundleAdditionalInfoTeaVm.get(bmf)!=null;});
		br.execute();
		int ret=0;
		try(BuildGenContext bgc=new BuildGenContext(cargs.bundleArgs.out, cargs.bundleArgs))
		{
			bgc.r=br.getResolver();
			if(cargs.bundlesToBuild.size()>0)
			{
				for(String s: cargs.bundlesToBuild)
				{
					HashSet<BundleManifest> bundles=bgc.r.allTobuildBundles.byId.get(s);
					if(bundles.size()!=1)
					{
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
		System.out.println("Call tea compiler on: "+b.id+" with "+e.state.resultBundles);
		BundleAdditionalInfoTeaVm tea=BundleAdditionalInfoTeaVm.get(b);
		File outputFile=new File(b.projectFile, tea.outputJsPath);

		TeaVMTool2.reflectiveExec(cargs, gatherTargetFiles(e.state.resultBundles), tea, outputFile);
	}
	@Override
	protected IArgs createArgsObject() {
		return new TeaVMCompileArgsBundle();
	}
}
