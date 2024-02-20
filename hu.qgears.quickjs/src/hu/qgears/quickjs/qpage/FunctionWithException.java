package hu.qgears.quickjs.qpage;

public interface FunctionWithException <X, Y>{
	Y apply(X x) throws Exception;
}
