package hu.qgears.quickjs.upload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.util.MultiMap;

import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.utils.IMultipartHandler;
import jakarta.servlet.ServletOutputStream;

public class UploadFileHandler extends HtmlTemplate implements IMultipartHandler
{
	private File uploadFolder;
	private String filename;
	private long filesize=-1;
	private long start=-1;
	private long end=-1;
	private boolean ok;
	class RAFOutput extends OutputStream
	{
		private RandomAccessFile raf;
		private long n=0;
		private String filename;
		private File f;
		public RAFOutput(String fileName, long start, long end) throws IOException {
			super();
			this.filename=fileName;
			f=new File(uploadFolder,fileName+".part");
			f.getParentFile().mkdirs();
			raf=new RandomAccessFile(f, "rw");
			raf.seek(start);
		}

		@Override
		public void write(int b) throws IOException {
			raf.write(b);
			n++;
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			raf.write(b, off, len);
			n+=len;
		}
		@Override
		public void close() throws IOException {
			raf.close();
			if(n!=end-start)
			{
				throw new IOException("Required number of bytes is: "+(end-start)+" received: "+n);
			}
			ok=true;
			if(end==filesize)
			{
				f.renameTo(new File(uploadFolder,filename));
			}
		}
		
	}
	public UploadFileHandler(File uplaodFolder) {
		this.uploadFolder=uplaodFolder;
	}
	@Override
	public OutputStream createPart(String name, String filename, MultiMap<String> headers, String contentType) throws IOException {
		switch(name)
		{
		case "filename": return new CloseEventByteArrayOutputStream(512, h->this.filename=new String(h, StandardCharsets.UTF_8));
		case "filesize": return new CloseEventByteArrayOutputStream(512, h->filesize=Long.parseLong(new String(h, StandardCharsets.UTF_8)));
		case "start": return new CloseEventByteArrayOutputStream(512, h->start=Long.parseLong(new String(h, StandardCharsets.UTF_8)));
		case "end": return new CloseEventByteArrayOutputStream(512, h->end=Long.parseLong(new String(h, StandardCharsets.UTF_8)));
		case "slice":
			if(start<0||end<0||filesize<0||filename==null)
			{
				throw new IOException("Missing mandatory parameters");
			}
			// System.out.println("File: '"+filename+"' at "+start+" Receiving N bytes: "+(end-start));
			return new BufferedOutputStream(new RAFOutput(validateFilename(this.filename), start, end));
		}
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				// Dummy - non handled parameters are omitted.
			}
		};
	}
	private String validateFilename(String filename) throws IOException {
		if(filename.indexOf("..")>=0 || filename.indexOf("/")>=0 ||filename.indexOf("\\")>=0)
		{
			throw new IOException("Invalid file name");
		}
		return filename;
	}
	
	public void writeResponse(ServletOutputStream outputStream) throws IOException {
		setWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
		if(ok)
		{
			write("this.at=");
			writeObject(end);
			write(";\nthis.progress(this.file, this.at);\n");
			if(filesize>end)
			{
				write("this.continue();\n");
			} else
			{
				write("this.finished(this.file, this.at);\n");
			}
		}else
		{
			write("this.error(this.file, \"ERROR\");\n");
		}
		getWriter().flush();
	}
}
