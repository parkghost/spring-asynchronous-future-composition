Below code snippet combine Guava ListenableFuture and spring @Async for non-blocking way to fetch process result and chaining.

### Configuration:

1. add ListeningThreadPoolTaskExecutor class to project classpath

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


2. add taskExecutor bean defintion to xml configuration
	
		<bean id="taskExecutor"
			class="org.springframework.scheduling.concurrent.ListeningThreadPoolTaskExecutor">
			<property name="corePoolSize" value="5" />
			<property name="maxPoolSize" value="10" />
			<property name="queueCapacity" value="25" />
		</bean>
	
		<task:annotation-driven executor="taskExecutor" />
	
	
### Example:

	@Service
	public class SearchService {
	
		@Async
		public Future<SearchResult> search(String keyword) {
			...
			return new AsyncResult<SearchResult>(…);
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
	
	
### Reference:
1.	[Google Guava ListenableFutureExplained](http://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained)
2.	[Spring Asynchronous Execution](http://static.springsource.org/spring/docs/3.0.x/reference/scheduling.html#scheduling-annotation-support-async)