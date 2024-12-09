package hu.qgears.quickjs.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Base64;

import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.quickjs.qpage.HtmlTemplate;

/**
 * Helper for Base64 image format.
 */
public class Base64Image {
	/**
	 * Create a string that can be used in a browser as a Base64 encoded image
	 * @param os The output string is written into this stream.
	 * @param mimetype mime type of the image (eg: image/png)
	 * @param mem image in native memory in mimetype encoding (eg image/png binary)
	 * @throws IOException
	 */
	public static void writeImage(Writer os, String mimetype, INativeMemory mem) throws IOException
	{
		os.write("data:");
		os.write(mimetype.toString());
		os.write(";base64,");
		try(OutputStream os2=createBase64Wrapper(os))
		{
			byte[] data=new byte[mem.getJavaAccessor().remaining()];
			mem.getJavaAccessor().get(data);
			os2.write(data);
		}
	}
	public static void writeImage(Writer os, String mimetype, byte[] data) throws IOException
	{
		os.write("data:");
		os.write(mimetype.toString());
		os.write(";base64,");
		try(OutputStream os2=createBase64Wrapper(os))
		{
			os2.write(data);
		}
	}
	/**
	 * Create a wrapper that converts byte[] data to Base64 string into the writer output. 
	 * @param wr
	 * @return
	 */
	public static OutputStream createBase64Wrapper(Writer wr)
	{
		OutputStream os2=Base64.getEncoder().wrap(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				wr.write(b);
			}
			@Override
			public void write(byte[] b) throws IOException {
				for(int i=0;i<b.length;++i)
				{
					wr.write(b[i]);
				}
			}
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				for(int i=0;i<len;++i)
				{
					wr.write(b[i+off]);
				}
			}
		});
		return os2;
	}
	/** write byte[] array to JS code that instantiates a blob based on that data.
	 * @param o
	 * @throws IOException 
	 */
	public static void writeBlobObject(HtmlTemplate template, byte[] o) {
		writeBlobObject(template, o, 0, o.length);
	}
	public static void writeBlobObject(HtmlTemplate template, byte[] o, int from, int length) {
		try {
			template.getWriter().write("await fetch('data:plain/text;base64,");
			try(OutputStream os=createBase64Wrapper(template.getWriter()))
			{
				os.write(o, from, length);
			}
			template.getWriter().write("').then(res => res.blob())\n");
		} catch (IOException e) {
			throw new RuntimeException("writing blob to JS code", e);
		}
	}
}
