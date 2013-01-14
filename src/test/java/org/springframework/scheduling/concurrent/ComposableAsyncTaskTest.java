package org.springframework.scheduling.concurrent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context.xml" })
public class ComposableAsyncTaskTest {
	@Autowired
	SampleService service;

	@Test
	public void testReturnListenableFuture() {
		Future<String> pong = service.ping();
		assertThat(pong, instanceOf(ListenableFuture.class));
	}

	@Test
	public void testSuccessfulResult() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		ListenableFuture<String> pong = (ListenableFuture<String>) service.ping();

		final AtomicReference<String> deferredResult = new AtomicReference<String>();
		FutureCallback<String> callback = new FutureCallback<String>() {
			public void onSuccess(String result) {
				deferredResult.set(result);
				latch.countDown();
			}

			public void onFailure(Throwable t) {
				// ignored
			}
		};

		Futures.addCallback(pong, callback);

		latch.await(2, TimeUnit.SECONDS);
		assertThat(deferredResult.get(), is("pong"));

	}

	@Test
	public void testFailureResult() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		ListenableFuture<String> boom = (ListenableFuture<String>) service.bang();

		final AtomicReference<String> deferredResult = new AtomicReference<String>();
		FutureCallback<String> callback = new FutureCallback<String>() {
			public void onSuccess(String result) {
				// ignored
			}

			public void onFailure(Throwable t) {
				latch.countDown();
			}
		};

		Futures.addCallback(boom, callback);

		latch.await(2, TimeUnit.SECONDS);
		assertThat(deferredResult.get(), is(nullValue()));

		Exception rootCause = null;
		try {
			boom.get();
			fail("should throw the boom excpetion");
		} catch (ExecutionException e) {
			rootCause = (Exception) e.getCause();
		}

		assertThat(rootCause, not(nullValue()));
		assertThat(rootCause, instanceOf(RuntimeException.class));
		assertThat(rootCause.getMessage(), is("boom!"));

	}
}
