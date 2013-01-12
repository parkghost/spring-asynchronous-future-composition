package org.springframework.scheduling.concurrent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
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
	public void testFutureCallback() throws InterruptedException {
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
}
