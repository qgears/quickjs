package hu.qgears.quickjs.teavm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.quickjs.helpers.IPlatform;
import hu.qgears.quickjs.helpers.Promise;
import hu.qgears.quickjs.helpers.QTimer;
import hu.qgears.quickjs.qpage.EQPageMode;
import hu.qgears.quickjs.qpage.HtmlTemplate;
import hu.qgears.quickjs.serialization.SerializeBase;

public class TeaVMPlatform  implements IPlatform {
	EQPageMode mode;
	private SerializeBase serialize;
	private TeaVMGui gui;
	public TeaVMPlatform(TeaVMGui gui, EQPageMode mode) {
		this.gui=gui;
		this.mode=mode;
	}

	@Override
	public QTimer startTimer(Runnable r, int firstTimeoutMs, int periodMs) {
		TeaVMTimer ret=new TeaVMTimer(gui, r, firstTimeoutMs, periodMs);
		return ret;
	}

	@Override
	public boolean isQPageThread() {
		return true;
	}

	@Override
	public void reinitDisposeTimer() {
		// Nothing to do in JS/TeaVM
	}

	@Override
	public int getTIMEOUT_DISPOSE_MS() {
		// 0 disables feature in JS/TeaVM
		return 0;
	}

	@Override
	public void startCommunicationWithJs() {
		// Nothing to do in JS/TeaVM
	}

	@Override
	public void disposeCommunicationToJS() {
		// Nothing to do in JS/TeaVM
	}

	@Override
	public void submitToUI(Runnable r) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <V> Promise<V> submitToUICallable(Callable<V> c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deregister() {
		// Nothing to do in JS/TeaVM
	}

	@Override
	public EQPageMode getMode() {
		return mode;
	}

	@Override
	public void writePreloadHeaders(HtmlTemplate parent) {
		// Nothing to do in JS/TeaVM
	}

	@Override
	public void writeHeaders(HtmlTemplate parent) {
		// Nothing to do in JS/TeaVM
	}

	@Override
	public List<String> getJsOrder() {
		// Feature not used on client side
		return new ArrayList<>();
	}

	@Override
	public String loadResource(String fname) throws Exception {
		// Feature not used on client side
		return "";
	}

	@Override
	public void configureJsGlobalQPage(HtmlTemplate parent, String string) {
		// Feature not used on client side
	}

	@Override
	public SerializeBase getSerializator() {
		return serialize;
	}

	@Override
	public void writeBlobObject(HtmlTemplate htmlTemplate, byte[] o) {
		throw new RuntimeException("Not implemented");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeBlobObject(HtmlTemplate htmlTemplate, byte[] o, int pos, int length) {
		throw new RuntimeException("Not implemented");
		// TODO Auto-generated method stub
		
	}
	public void setSerializator(SerializeBase serialize) {
		this.serialize=serialize;
	}

	@Override
	public boolean isServer() {
		return false;
	}

	@Override
	public List<byte[]> getReplayObjects() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setSetupContext(Supplier<NoExceptionAutoClosable> setupContext) {
		// TODO Auto-generated method stub
	}
}
