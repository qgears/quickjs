package hu.qgears.quickjs.utils;

public interface NoExceptionAutoClosable extends AutoCloseable
{
	@Override
	default void close() {
	}
}
