package hu.qgears.quickjs.helpers;

import java.util.function.Consumer;
import java.util.function.Function;

public class PromiseImpl<T> implements Promise<T>
{
	@Override
	public <Q> Promise<Q> thenAccept(Function<T, Q> acceptResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Promise<Void> thenApply(Consumer<T> applyResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Q> Promise<Q> process(Function<Result<T>, Q> processor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Promise<T> CatchThrowable(Function<Throwable, T> handler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Promise<T> CatchException(Function<Exception, T> handler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Promise<T> onError(Function<Exception, T> handler) {
		// TODO Auto-generated method stub
		return null;
	}
}
