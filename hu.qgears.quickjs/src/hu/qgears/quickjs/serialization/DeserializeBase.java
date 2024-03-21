package hu.qgears.quickjs.serialization;

public class DeserializeBase {
	public Object deserialize()
	{
		String typeName=readString();
		Object ret=null;
		switch(typeName)
		{
		case "null":
			break;
		case "java.util.ArrayList":
		default:
			throw new RuntimeException("null");
		}
		return ret;
	}

	private String readString() {
		// TODO Auto-generated method stub
		return null;
	}
}
