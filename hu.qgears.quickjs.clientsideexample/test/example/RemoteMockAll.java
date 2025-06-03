package example;

public class RemoteMockAll implements All{
	@Override
	public ExampleRemoteIf getRemote() {
		return new RemoteMock();
	}
}
