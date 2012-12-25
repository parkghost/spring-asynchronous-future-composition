Below code snippet combine Guava ListenableFuture and spring @Async for non-blocking way to fetch process result and chaining.

### Configuration:

1\. add `ListeningThreadPoolTaskExecutor` class to project classpath

```java
package org.springframework.scheduling.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.core.task.TaskRejectedException;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class ListeningThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

	@Override
	public Future<?> submit(Runnable task) {
		ListeningExecutorService executor = MoreExecutors.listeningDecorator(getThreadPoolExecutor());

		try {
			return executor.submit(task);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		ListeningExecutorService executor = MoreExecutors.listeningDecorator(getThreadPoolExecutor());

		try {
			return executor.submit(task);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}
}
```
2\. add `taskExecutor` bean defintion to xml configuration
	
```xml
<bean id="taskExecutor"
	class="org.springframework.scheduling.concurrent.ListeningThreadPoolTaskExecutor">
	<property name="corePoolSize" value="5" />
	<property name="maxPoolSize" value="10" />
	<property name="queueCapacity" value="25" />
</bean>

<task:annotation-driven executor="taskExecutor" />
````	
	
### Example:

```java
@Service
public class SearchService {

	@Async
	public Future<SearchResult> search(String keyword) {
		SearchResult result = ... //do some blocking or heavy operation here
		return new AsyncResult<SearchResult>(result);
	}
	
}

@Service
public class SearchApplication {

	public void runWithFutureCallback() {	
		ListenableFuture<SearchResult> future1 = (ListenableFuture<SearchResult>) searchService.search("Joshua Bloch");
		
		Futures.addCallback(future1, new FutureCallback<SearchResult>() {
			public void onSuccess(SearchResult result) {
				logger.debug("search keyword:{} found:{} totalTime:{}ms", new Object[] { result.getKeyword(), result.getRecords(), result.getTotalTime() });
			}
	
			public void onFailure(Throwable thrown) {
				logger.warn(thrown.getMessage());
			}
		});
	}
	
}
```

For <a href="spring-asynchronous-future-composition/tree/master/src/main/java/me/brandon/application">full example</a>, please look in the `src/main/java/me/brandon/application` directory.

*the console output from full example*

	2012-11-29 14:26:01,045 [main] INFO  me.brandon.application.Main - starting application
	2012-11-29 14:26:01,869 [taskExecutor-1] DEBUG me.brandon.application.SearchService - search keyword:Joshua Bloch
	2012-11-29 14:26:01,869 [taskExecutor-3] DEBUG me.brandon.application.SearchService - search keyword:Joshua Bloch
	2012-11-29 14:26:01,869 [taskExecutor-4] DEBUG me.brandon.application.SearchService - search keyword:Martin Odersky
	2012-11-29 14:26:01,869 [taskExecutor-5] DEBUG me.brandon.application.SearchService - search keyword:Brian Goetz
	2012-11-29 14:26:01,869 [taskExecutor-2] DEBUG me.brandon.application.SearchService - search keyword:Martin Fowler
	2012-11-29 14:26:02,874 [taskExecutor-3] DEBUG me.brandon.application.SearchService - search keyword:Bruce Eckel
	2012-11-29 14:26:03,871 [taskExecutor-5] DEBUG me.brandon.application.SearchService - search keyword:Martin Fowler
	2012-11-29 14:26:03,871 [taskExecutor-1] DEBUG me.brandon.application.SearchApplication - search keyword:Joshua Bloch found:[Effective Java, Java Concurrency in Practice, JavaTM Puzzlers, Java Concurrency in Practice] totalTime:2002ms
	2012-11-29 14:26:09,875 [taskExecutor-5] DEBUG me.brandon.application.SearchApplication - search keyword:Joshua BlochMartin OderskyBrian GoetzBruce Eckel found:[Thinking in C++, JavaTM Puzzlers, Thinking in Java, Programming in Scala, Effective Java, Java Concurrency in Practice] totalTime:7001ms
	2012-11-29 14:26:10,870 [taskExecutor-2] WARN  me.brandon.application.SearchApplication - the keyword 'Martin Fowler'did not match any records
	2012-11-29 14:26:10,871 [main] INFO  me.brandon.application.Main - leaving application

	
### Reference:
1.	[Google Guava ListenableFutureExplained](http://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained)
2.	[Spring Asynchronous Execution](http://static.springsource.org/spring/docs/3.0.x/reference/scheduling.html#scheduling-annotation-support-async)
