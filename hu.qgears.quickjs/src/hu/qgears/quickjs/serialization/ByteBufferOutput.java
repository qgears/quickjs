package hu.qgears.quickjs.serialization;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

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
		}else
		{
			while(wrap.position()+i>output.length)
			{
				int pos=wrap.position();
				output=Arrays.copyOf(output, output.length*2);
				wrap=ByteBuffer.wrap(output);
				wrap.order(ByteOrder.LITTLE_ENDIAN);
				wrap.position(pos);
			}
		}
	}
	public void writeString(String s) {
		if(s==null)
		{
			ensureAppendSize(4);
			wrap.putInt(-1);
			return;
		}
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
	public byte[] getData() {
		return output;
	}
	public int getLength() {
		return wrap.position();
	}
	public void writeLong(long l) {
		ensureAppendSize(8);
		wrap.putLong(l);
	}
	public byte[] getDataCopy() {
		return Arrays.copyOf(output, wrap.position());
	}
	public void writeBool(boolean b) {
		ensureAppendSize(1);
		wrap.put((byte) (b?1:0));
	}
}
