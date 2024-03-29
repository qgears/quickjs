package hu.qgears.quickjs.serialization;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferOutput {
	private final static int initialsize=1024;
	byte[] output;
	ByteBuffer wrap;
	
	public void writeInt(int size) {
		ensureAppendSize(4);
		wrap.putInt(size);
	}
	private void ensureAppendSize(int i) {
		if(output==null)
		{
			output=new byte[initialsize];
			wrap=ByteBuffer.wrap(output);
			wrap.order(ByteOrder.LITTLE_ENDIAN);
		}
	}
	public void writeString(String s) {
		ensureAppendSize(s.length()*2+4);
		wrap.putInt(s.length());
		for(int i=0;i<s.length();++i)
		{
			char ch=s.charAt(i);
			wrap.putShort((short)ch);
		}
	}
	public void reset() {
		if(wrap!=null)
		{
			wrap.clear();
		}
	}
	public ByteBuffer getSerializedBinary() {
		wrap.flip();
		return wrap;
	}
}
