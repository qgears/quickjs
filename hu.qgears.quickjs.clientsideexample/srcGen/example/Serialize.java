package example;
public class Serialize extends hu.qgears.quickjs.serialization.SerializeBase
{
	public boolean serializeObject(Object value)
	{
		String className=value==null?"null":value.getClass().getName();
		switch(className)
		{
			case "example.ExampleSerializableObject":
			{
				writeString(className);
				writeString("a");
				serializeObject(((example.ExampleSerializableObject)value).a);
				writeString("b");
				serializeObject(((example.ExampleSerializableObject)value).b);
				writeString("c");
				serializeObject(((example.ExampleSerializableObject)value).c);
				writeString("getD");
				serializeObject(((example.ExampleSerializableObject)value).getD());
				return true;
			}
			default:
				return super.serializeObject(value);
		}
	}
	public Object deserializeObject(String type)
	{
		switch(type)
		{
			case "example.ExampleSerializableObject":
			{
				example.ExampleSerializableObject ret=new example.ExampleSerializableObject();
				assertEqual("a", readString());
				ret.a=(java.lang.Integer) deserializeObject();
				assertEqual("b", readString());
				ret.b=(java.lang.String) deserializeObject();
				assertEqual("c", readString());
				ret.c=(java.lang.Long) deserializeObject();
				assertEqual("getD", readString());
				ret.setD((java.lang.Integer) deserializeObject());
				return ret;
			}
			default:
				return super.deserializeObject(type);
		}
	}
}
