package hu.qgears.quickjs.helpers;

public class Result<T> {
	private T result;
	private Throwable t;
	public Result(T result, Throwable t) {
		super();
		this.result = result;
		this.t = t;
	}
	public T getResult() {
		return result;
	}
	public boolean isError()
	{
		return t!=null;
	}
	public Throwable getThrowable() {
		return t;
	}
}
