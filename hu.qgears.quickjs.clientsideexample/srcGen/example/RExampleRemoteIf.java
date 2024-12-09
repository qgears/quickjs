package example;
public interface RExampleRemoteIf {
	public hu.qgears.quickjs.helpers.Promise<java.util.List<java.lang.String>> findAll (java.lang.String arg0);
	public hu.qgears.quickjs.helpers.Promise<example.ExampleSerializableObject> testit ();
	public hu.qgears.quickjs.helpers.Promise<java.lang.Void> mycallback (hu.qgears.quickjs.serialization.IRemotingCallback<java.lang.String> arg1);
	public hu.qgears.quickjs.helpers.Promise<java.lang.Void> masik (java.util.Set<java.lang.Integer> arg0);
	public hu.qgears.quickjs.helpers.Promise<java.util.Map<java.lang.String, java.lang.Integer>> harmadik (java.util.Set<java.lang.Integer> arg0, java.lang.String arg1);
	public hu.qgears.quickjs.helpers.Promise<java.lang.Integer> alma (int arg0);
	public hu.qgears.quickjs.helpers.Promise<java.lang.Integer> alma (java.lang.String arg0);
}
