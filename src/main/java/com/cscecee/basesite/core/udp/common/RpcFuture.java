package com.cscecee.basesite.core.udp.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RpcFuture implements Future {

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
		latch.await(10, TimeUnit.SECONDS);
		if (error != null) {
			throw new ExecutionException(error);
		}
		return result;
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
