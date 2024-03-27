package hu.qgears.quickjs.helpers;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The promise concept implemented in Java.
 */
public interface Promise<T> {
	/**
	 * Handler to execute once the promise is fulfilled with correct value.
	 * @return new promise with this handler chained
	 */
	<Q> Promise<Q> thenAccept(Function<T, Q> acceptResult); 
	/**
	 * Function to execute once the promise is fulfilled with correct value.
	 * @param applyResult
	 * @return new promise with this handler chained
	 */
	Promise<Void> thenApply(Consumer<T> applyResult);
	/**
	 * Process the result object (correct value or exception).
	 * @param <Q>
	 * @param processor
	 * @return
	 */
	<Q> Promise<Q> process(Function<Result<T>, Q> processor);
	/**
	 * In case of the promise returns with a throwable
	 * then this catch handler is executed and its result will be the new result of this promise. 
	 * @param handler
	 * @return new promise with this catcher chained
	 */
	Promise<T> CatchThrowable(Function<Throwable, T> handler);
	/**
	 * In case of the promise returns with an exception
	 * then this catch handler is executed and its result will be the new result of this promise. 
	 * @param handler
	 * @return new promise with this catcher chained
	 */
	Promise<T> CatchException(Function<Exception, T> handler);
	/**
	 * In case of the promise returns with an exception
	 * then this handler is executed but the original promise result stays to be an exception. 
	 * @param handler
	 * @return new promise with the error handler chained
	 */
	<Q> Promise<Q> onError(Function<Exception, Q> handler);
	/**
	 * Accept result (T object or throwable)
	 * @param resultHandler
	 */
	void resultAccept(Consumer<Result<T>> resultHandler);
}
