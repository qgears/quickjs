package hu.qgears.quickjs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.Request;

import hu.qgears.commons.UtilFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FolderHandler extends AbstractResourceHandler {
	private File folder;
	
	public FolderHandler(File folder) {
		super();
		this.folder = folder;
		allowFolderListing=true;
	}

	@Override
	protected InputStreamSupplier getFileOpener(String resname, String acceptEncoding) {
		HttpPath path=new HttpPath(resname);
		String ext=path.getExtension();
		validateExtension(ext);
		File g=new File(folder, path.toStringPath());
		if(g.isFile())
		{
			return new InputStreamSupplier() {
				public java.io.InputStream open() throws IOException {
					FileInputStream fis=new FileInputStream(g);
					return fis;
				}
				@Override
				public boolean supportsRange() {
					return true;
				}
				@Override
				public InputStream openRange(long offsetFrom, long offsetTo) throws IOException {
					FileInputStream fis=new FileInputStream(g);
					if(offsetFrom!=0)
					{
						HelperInputStream.skipNBytes(fis, offsetFrom);
					}
					return fis;
				}
				@Override
				public boolean supportsLength() {
					return true;
				}
				@Override
				public long length() {
					return g.length();
				}
			};
		}
		return null;
	}
	@Override
	protected void renderFolderListing(HttpPath path, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		File dir=folder;
		for(String s: path.getPieces())
		{
			dir=new File(dir, s);
		}
		List<String> names=UtilFile.listFiles(dir).stream().map(f->
		f.isDirectory()? (f.getName()+"/") : f.getName()).collect(Collectors.toList());
		new ListingRenderer(names)
			.handle(path.toStringPath(), baseRequest, request, response);
	}
}
