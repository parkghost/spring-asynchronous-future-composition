package org.springframework.scheduling.concurrent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

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
	public void testFutureCallback() {
		final CountDownLatch latch = new CountDownLatch(1);
		ListenableFuture<String> pong = (ListenableFuture<String>) service.ping();

		FutureCallback<String> callback = new FutureCallback<String>() {
			public void onSuccess(String result) {
				assertThat(result, is("pong"));
				latch.countDown();
			}

			public void onFailure(Throwable t) {
				// ignored
			}
		};

		Futures.addCallback(pong, callback);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
