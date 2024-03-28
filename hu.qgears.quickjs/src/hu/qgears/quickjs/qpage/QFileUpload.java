package hu.qgears.quickjs.qpage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEvent;
import hu.qgears.quickjs.qpage.IndexedComm.Msg;

/**
 * File upload handler.
 */
public class QFileUpload extends QComponent
{
	public interface FolderCreator
	{
		void createFolder(QFileUpload fu, String folderPath);
	}
	public class FileInfo
	{
		public final String name;
		public final long size;
		public FileInfo(String name, long size) {
			super();
			this.name = name;
			this.size = size;
		}
	}
	private OutputStream bos;
	private String currentFileName;
	private long at;
	private long size;
	private long allReceived;
	private long allEnqueued;
	private String jsRef;
	public final UtilEvent<QFileUpload> statusUpdated=new UtilEvent<>();
	private IndexedComm comm=new IndexedComm();
	private FolderCreator folderCreator;
	private FunctionWithException<QFileUpload, OutputStream> outputStreamCreator=q->new ByteArrayOutputStream();
	private String commId;
	public QFileUpload(IQContainer container, String id) {
		super(container, id);
		initCommListeners();
		init();
	}

	public QFileUpload(IQContainer container) {
		super(container);
		initCommListeners();
		init();
	}
	private void initCommListeners() {
		comm.received.addListener(msg->{
			received(msg);
		});
		if(getPage()!=null)
		{
			commId=getPage().registerCustomWebsocketImplementation(comm);
		}
	}
	private Map<String, FileInfo> toUploads=new TreeMap<>();
	private void received(Msg msg)
	{
		JSONObject o=(JSONObject)msg.header;
		String type=""+o.getString("type");
		switch(type)
		{
		default:
			System.err.println("QFileUpload unhandled: "+o.getString("type"));
			break;
		case "createFolder":
			if(folderCreator!=null)
			{
				folderCreator.createFolder(this, o.getString("filename"));
			}
			break;
		case "enqueue":
			FileInfo fi=new FileInfo(o.getString("filename"), o.getLong("filesize"));
			toUploads.put(fi.name, fi);
			allEnqueued+=fi.size;
			break;
		case "newfile":
		{
			if(bos!=null)
			{
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bos=null;
			}
			String fileName=o.getString("filename");
			long fileLength=o.getLong("filesize");
			currentFileName=fileName;
			size=fileLength;
			at=0;
			try {
				bos=outputStreamCreator.apply(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getPage().submitToUI(new Runnable() {
				@Override
				public void run() {
					statusUpdated.eventHappened(QFileUpload.this);
				}
			});
			break;
		}
		case "slice":
		{
			byte[] slice=msg.arguments.length>0?((byte[])msg.arguments[0]):null;
			long fileLength=o.getLong("filesize");
			long at=o.getLong("at");
			at+=slice.length;
			this.allReceived+=slice.length;
			this.at=at;
			comm.sendMessage("{\"type\": \"received\", \"received\": "+slice.length+"}");
			try {
				bos.write(slice);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(at>=fileLength)
			{
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bos=null;
				System.out.println("File upload finsihed: "+currentFileName+" "+at+" "+fileLength);
			}
			getPage().submitToUI(new Runnable() {
				@Override
				public void run() {
					statusUpdated.eventHappened(QFileUpload.this);
				}
			});
			break;
		}
		}
	}

//	@Override
//	public void generateHtmlObject() {
//		write("<input id=\"");
//		writeObject(id);
//		write("\" type=\"file\" name=\"attachment[]\" webkitdirectory directory multiple>\n");
//	}

	@Override
	protected void doInitJSObject() {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("\tnew QFileUpload(page, \"");
			writeObject(id);
			write("\", \"");
			writeObject(commId);
			write("\");\n");
		}
		if(jsRef!=null)
		{
			installDropListenerPrivate(jsRef);
			jsRef=null;
		}
	}

	@Override
	public void handle(JSONObject post) throws IOException {
	}
	public boolean isFileReady()
	{
		return at==size;
	}

	public String getFileName() {
		return currentFileName;
	}

	public long getFileSize() {
		return size;
	}
	public long getAt() {
		return at;
	}
	public OutputStream getReceivedStream() {
		return bos;
	}
	/**
	 * Set the output of the file upload.
	 * @param outputStreamCreator
	 */
	public void setOutputStreamCreator(FunctionWithException<QFileUpload, OutputStream> outputStreamCreator) {
		this.outputStreamCreator = outputStreamCreator;
	}
	@Override
	protected void onDispose() {
		if(getPage()!=null)
		{
			getPage().unregisterCustomWebsocketImplementation(commId);
		}
		if(bos!=null)
		{
			try {
				bos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bos=null;
		}
		super.onDispose();
	}

	public void installDropListener(String string) {
		if(this.inited)
		{
			installDropListenerPrivate(string);
		}else
		{
			jsRef=string;
		}
	}
	private void installDropListenerPrivate(String string) {
		try(NoExceptionAutoClosable c=activateJS())
		{
			write("globalQPage.components[\"");
			writeObject(id);
			write("\"].installDrop(");
			writeObject(string);
			write(");\n");
		}
	}
	public QFileUpload setFolderCreator(FolderCreator folderCreator) {
		this.folderCreator = folderCreator;
		return this;
	}
	public long getAllReceived() {
		return allReceived;
	}
	public Map<String, FileInfo> getToUploads() {
		return toUploads;
	}
	public long getAllEnqueued() {
		return allEnqueued;
	}
	@Override
	protected boolean isSelfInitialized() {
		return true;
	}
}
