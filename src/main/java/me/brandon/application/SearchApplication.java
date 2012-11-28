package me.brandon.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

@Service
public class SearchApplication {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final CountDownLatch countDownLatch = new CountDownLatch(2);

	private final FutureCallback<SearchResult> showResult = new FutureCallback<SearchResult>() {
		public void onSuccess(SearchResult result) {
			logger.debug("search keyword:{} found:{} totalTime:{}ms", new Object[] { result.getKeyword(), result.getRecords(), result.getTotalTime() });
		}

		public void onFailure(Throwable thrown) {
			logger.warn(thrown.getMessage());
		}
	};

	@Autowired
	private SearchService searchService;

	public void runWithFutureCallback() {

		// asynchronous execution
		ListenableFuture<SearchResult> future1 = (ListenableFuture<SearchResult>) searchService.search("Joshua Bloch");

		// !!! the keyword 'Martin Fowler' does not exist in the database
		ListenableFuture<SearchResult> future2 = (ListenableFuture<SearchResult>) searchService.search("Martin Fowler");

		// try callback without polling
		Futures.addCallback(future1, showResult);
		Futures.addCallback(future2, showResult);

		asyncCountDown(future1, future2);
	}

	public void runWithAsyncFunction() {

		// scatter-gather search
		List<String> terms = Arrays.asList(new String[] { "Joshua Bloch", "Martin Odersky", "Brian Goetz", "Bruce Eckel", "Martin Fowler" });
		List<ListenableFuture<SearchResult>> futures = new ArrayList<ListenableFuture<SearchResult>>();
		for (String term : terms) {
			futures.add((ListenableFuture<SearchResult>) searchService.search(term));
		}

		// collect all of successful search results and merge
		AsyncFunction<List<SearchResult>, SearchResult> flattenFunction = new AsyncFunction<List<SearchResult>, SearchResult>() {
			public ListenableFuture<SearchResult> apply(List<SearchResult> results) {
				StringBuilder keywords = new StringBuilder();
				Set<String> records = new HashSet<String>();
				long startTime = Long.MAX_VALUE;
				long endTime = 0;

				for (SearchResult result : results) {
					if (result != null) {
						keywords.append(result.getKeyword());
						records.addAll(result.getRecords());

						startTime = Math.min(startTime, result.getStartTime());
						endTime = Math.max(endTime, result.getEndTime());
					}
				}

				SettableFuture<SearchResult> constFuture = SettableFuture.create();
				constFuture.set(new SearchResult(keywords.toString(), new ArrayList<String>(records), startTime, endTime));
				return constFuture;
			}
		};

		ListenableFuture<List<SearchResult>> collectedResults = Futures.successfulAsList(futures);

		ListenableFuture<SearchResult> mergedResult = Futures.transform(collectedResults, flattenFunction);

		Futures.addCallback(mergedResult, showResult);

		asyncCountDown(mergedResult);
	}

	public void await() throws InterruptedException {
		countDownLatch.await();
	}

	public <V> void asyncCountDown(ListenableFuture<? extends V>... futures) {
		Futures.successfulAsList(futures).addListener(new Runnable() {
			public void run() {
				countDownLatch.countDown();
			}
		}, MoreExecutors.sameThreadExecutor());
	}

}
