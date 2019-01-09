package com.cscecee.basesite.core.udp.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RpcFuture implements Future {

	private int timeOut = 10;//10 秒
	
	private byte[] result;
	
	private Throwable error;
	
	private CountDownLatch latch = new CountDownLatch(1);

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return result != null || error != null;
	}

	public void success(byte[] result) {
		this.result = result;
		latch.countDown();
	}

	public void fail(Throwable error) {
		this.error = error;
		latch.countDown();
	}

	/**
	 * 等待10秒超时时间
	 */
	@Override
	public byte[] get() throws InterruptedException, ExecutionException {
		latch.await(timeOut, TimeUnit.SECONDS);
		if (error != null) {
			throw new ExecutionException(error);
		}
		return result;
	}
	/**
	 * 等待toal*10秒超时时间
	 */
	public byte[] get(int totalFragment) throws InterruptedException, ExecutionException, TimeoutException {
		return get(timeOut * totalFragment, TimeUnit.SECONDS);
	}

	@Override
	public byte[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		latch.await(timeout, unit);
		if (error != null) {
			throw new ExecutionException(error);
		}
		return result;
	}

}
