package hu.qgears.quickjs.utils;

import java.util.concurrent.Future;

import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.quickjs.helpers.Promise;

public class PromiseToFuture<T> extends SignalFutureWrapper<T> implements Future<T>{
	public PromiseToFuture(Promise<T> promise)
	{
		promise.listenResult((result)->{
			ready(result.getResult(), result.getThrowable());
		});
	}
}
