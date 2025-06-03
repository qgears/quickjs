package hu.qgears.quickjs.helpers;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromiseImpl<T> implements Promise<T>
{
	private static Logger log=LoggerFactory.getLogger(PromiseImpl.class);
	private Consumer<?>[] consumers=(Consumer<?>[])new Consumer<?>[0];
	private volatile Result<T> result=null;
	@Override
	public <Q> Promise<Q> thenAccept(Function<T, Q> acceptResult) {
		PromiseImpl<Q> ret=new PromiseImpl<>();
		addConsumer(r->{
					Result<T> resin=(Result<T>) r;
					if(resin.getThrowable()==null)
					{
						try {
							Q processed=acceptResult.apply(resin.getResult());
							ret.ready(processed);
						} catch (Exception e) {
							ret.error(e);
						}
						ret.ready(null);
					}else
					{
						ret.error(resin.getThrowable());
					}
				});
		return ret;
	}
	/**
	 * Add consumer to the consumers list.
	 * In case the result is already ready then apply it onto the consumer
	 * instead of adding it the the list of listener consumers
	 * @param consumer
	 * @return
	 */
	private void addConsumer(Consumer<Result<T>> consumer)
	{
		Result<T> res=null;
		synchronized (this) {
			if(result==null)
			{
				consumers=Arrays.copyOf(this.consumers, this.consumers.length+1);
				consumers[consumers.length-1]=consumer;
			}else
			{
				res=result;
			}
		}
		if(res!=null)
		{
			consumer.accept(res);
		}
	}

	@Override
	public Promise<Void> thenApply(Consumer<T> applyResult) {
		PromiseImpl<Void> ret=new PromiseImpl<>();
		addConsumer(r->{
					Result<T> resin=(Result<T>) r;
					if(resin.getThrowable()==null)
					{
						try {
							applyResult.accept(resin.getResult());
						} catch (Exception e) {
							log.error("thenApply", e);
						}
						ret.ready(null);
					}else
					{
						ret.error(resin.getThrowable());
					}
				});
		return ret;
	}
	@Override
	public <Q> Promise<Q> process(Function<Result<T>, Q> processor) {
		throw new RuntimeException("TODO not implemented");
	}
	@Override
	public Promise<T> CatchThrowable(Function<Throwable, T> handler) {
		throw new RuntimeException("TODO not implemented");
	}
	@Override
	public Promise<T> CatchException(Function<Exception, T> handler) {
		throw new RuntimeException("TODO not implemented");
	}
	@Override
	public <Q> Promise<Q> onError(Function<Exception, Q> handler) {
		PromiseImpl<Q> ret=new PromiseImpl<>();
		addConsumer(r->{
			Result<T> resin=(Result<T>) r;
			Throwable t=resin.getThrowable();
			if(t!=null && t instanceof Exception)
			{
				Exception e=(Exception) t;
				try {
					Q q=handler.apply(e);
					ret.ready(q);
				} catch (Exception ex) {
					log.error("onError", ex);
				}
				ret.ready(null);
			}else
			{
				ret.error(resin.getThrowable());
			}
		});
		return ret;
	}
	public void ready(T o) {
		setResult(new Result<T>(o, null));
	}
	@SuppressWarnings("unchecked")
	private void setResult(Result<T> result) {
		Consumer<?>[] consumers;
		synchronized (this) {
			if(this.result!=null)
			{
				return;
			}
			this.result=result;
			consumers=this.consumers;
		}
		for(Consumer<?> c: consumers)
		{
			((Consumer<Result<T>>)c).accept(result);
		}
	}

	@Override
	public void error(Throwable throwable) {
		setResult(new Result<T>(null, throwable));
	}
	@Override
	public void resultAccept(Consumer<Result<T>> resultHandler) {
		addConsumer(resultHandler);
	}
	@Override
	public T getValueSync() {
		if(result==null)
		{
			throw new IllegalStateException("Promise result is not available yet");
		}else if(result.getThrowable()!=null)
		{
			throw new PromiseException(result.getThrowable());
		}else
		{
			return result.getResult();
		}
	}
	public boolean isDone()
	{
		return result!=null;
	}
	@Override
	public Promise<T> listenResult(Consumer<Result<T>> resultListener) {
		addConsumer(resultListener);
		return this;
	}
}
