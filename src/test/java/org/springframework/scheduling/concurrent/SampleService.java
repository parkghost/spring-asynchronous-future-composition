package org.springframework.scheduling.concurrent;

import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class SampleService {
	@Async
	public Future<String> ping() {
		return new AsyncResult<String>("pong");
	}

	@Async
	public Future<String> bang() {
		throw new RuntimeException("boom!");
	}
}
