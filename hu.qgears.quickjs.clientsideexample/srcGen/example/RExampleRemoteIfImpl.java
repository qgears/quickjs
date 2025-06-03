package example;
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RExampleRemoteIfImpl extends hu.qgears.quickjs.serialization.RemotingBase implements example.RExampleRemoteIf{
	@Override
	public hu.qgears.quickjs.helpers.Promise<java.util.List<java.lang.String>> findAll (java.lang.String arg0)	{
		String methodPrototype="findAll(java.lang.String)";
		Object[] argumentsToSerialize=new Object[]{
		arg0 };
		hu.qgears.quickjs.helpers.PromiseImpl<java.util.List<java.lang.String>> remotingReturnValue = new hu.qgears.quickjs.helpers.PromiseImpl<java.util.List<java.lang.String>>();
		executeRemoteCall(java.util.List.class,
			(hu.qgears.quickjs.helpers.PromiseImpl<java.util.List>)(Object) remotingReturnValue,
			"getRemote", methodPrototype, argumentsToSerialize, false);
		return remotingReturnValue;
	}
	@Override
	public hu.qgears.quickjs.helpers.Promise<example.ExampleSerializableObject> testit ()	{
		String methodPrototype="testit()";
		Object[] argumentsToSerialize=new Object[]{
		 };
		hu.qgears.quickjs.helpers.PromiseImpl<example.ExampleSerializableObject> remotingReturnValue = new hu.qgears.quickjs.helpers.PromiseImpl<example.ExampleSerializableObject>();
		executeRemoteCall(example.ExampleSerializableObject.class,
			(hu.qgears.quickjs.helpers.PromiseImpl<example.ExampleSerializableObject>)(Object) remotingReturnValue,
			"getRemote", methodPrototype, argumentsToSerialize, false);
		return remotingReturnValue;
	}
	@Override
	public hu.qgears.quickjs.helpers.Promise<java.lang.Void> mycallback (hu.qgears.quickjs.serialization.IRemotingCallback<java.lang.String> arg1)	{
		String methodPrototype="mycallback(hu.qgears.quickjs.serialization.IRemotingCallback)";
		Object[] argumentsToSerialize=new Object[]{
		registerCallback(methodPrototype, arg1)
 };
		hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Void> remotingReturnValue = new hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Void>();
		executeRemoteCall(java.lang.Void.class,
			(hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Void>)(Object) remotingReturnValue,
			"getRemote", methodPrototype, argumentsToSerialize, true);
		return remotingReturnValue;
	}
	@Override
	public hu.qgears.quickjs.helpers.Promise<java.lang.Void> masik (java.util.Set<java.lang.Integer> arg0)	{
		String methodPrototype="masik(java.util.Set)";
		Object[] argumentsToSerialize=new Object[]{
		arg0 };
		hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Void> remotingReturnValue = new hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Void>();
		executeRemoteCall(java.lang.Void.class,
			(hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Void>)(Object) remotingReturnValue,
			"getRemote", methodPrototype, argumentsToSerialize, false);
		return remotingReturnValue;
	}
	@Override
	public hu.qgears.quickjs.helpers.Promise<java.util.Map<java.lang.String, java.lang.Integer>> harmadik (java.util.Set<java.lang.Integer> arg0, java.lang.String arg1)	{
		String methodPrototype="harmadik(java.util.Set,java.lang.String)";
		Object[] argumentsToSerialize=new Object[]{
		arg0, arg1 };
		hu.qgears.quickjs.helpers.PromiseImpl<java.util.Map<java.lang.String, java.lang.Integer>> remotingReturnValue = new hu.qgears.quickjs.helpers.PromiseImpl<java.util.Map<java.lang.String, java.lang.Integer>>();
		executeRemoteCall(java.util.Map.class,
			(hu.qgears.quickjs.helpers.PromiseImpl<java.util.Map>)(Object) remotingReturnValue,
			"getRemote", methodPrototype, argumentsToSerialize, false);
		return remotingReturnValue;
	}
	@Override
	public hu.qgears.quickjs.helpers.Promise<java.lang.Integer> alma (int arg0)	{
		String methodPrototype="alma(int)";
		Object[] argumentsToSerialize=new Object[]{
		arg0 };
		hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Integer> remotingReturnValue = new hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Integer>();
		executeRemoteCall(java.lang.Integer.class,
			(hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Integer>)(Object) remotingReturnValue,
			"getRemote", methodPrototype, argumentsToSerialize, false);
		return remotingReturnValue;
	}
	@Override
	public hu.qgears.quickjs.helpers.Promise<java.lang.Integer> alma (java.lang.String arg0)	{
		String methodPrototype="alma(java.lang.String)";
		Object[] argumentsToSerialize=new Object[]{
		arg0 };
		hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Integer> remotingReturnValue = new hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Integer>();
		executeRemoteCall(java.lang.Integer.class,
			(hu.qgears.quickjs.helpers.PromiseImpl<java.lang.Integer>)(Object) remotingReturnValue,
			"getRemote", methodPrototype, argumentsToSerialize, false);
		return remotingReturnValue;
	}
}
