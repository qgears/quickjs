package example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.quickjs.serialization.RemotingImplementationMock;

public class TryRemoting {
	@Test
	public void test01() throws InterruptedException, ExecutionException, TimeoutException {
		RemoteMock hello=new RemoteMock();
		Remoting r=new Remoting();
		RemotingImplementationMock ri=new RemotingImplementationMock();
		RemotingServer rs=new RemotingServer();
		ri.setRemotingServer(rs);
		rs.setRpublishedObject(hello);
		Serialize s=new Serialize();
		ri.setSerialize(s);
		r.setRemotingImplementation(ri);
		SignalFutureWrapper<Integer> ret=new SignalFutureWrapper<>();
		r.alma("korte").thenApply(
			v->{
				ret.ready(v, null);
			}).resultAccept(v->{});
		Integer v=ret.get(1, TimeUnit.SECONDS);
		Assert.assertEquals(5, (int)v);
	}
}
