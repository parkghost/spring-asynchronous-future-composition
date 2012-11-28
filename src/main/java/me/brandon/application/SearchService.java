package me.brandon.application;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, List<String>> database = new HashMap<String, List<String>>();

	SearchService() {
		database.put("Joshua Bloch", Arrays.asList("Effective Java", "Java Concurrency in Practice", "JavaTM Puzzlers", "Java Concurrency in Practice"));
		database.put("Martin Odersky", Arrays.asList("Programming in Scala"));
		database.put("Brian Goetz", Arrays.asList("Java Concurrency in Practice"));
		database.put("Bruce Eckel", Arrays.asList("Thinking in Java", "Thinking in C++"));
	}

	@Async
	public Future<SearchResult> search(String keyword) {
		logger.debug("search keyword:{}", keyword);
		long startTime = System.currentTimeMillis();

		try {
			// simulate network latency
			Thread.sleep(ThreadLocalRandom.current().nextInt(10) * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		List<String> records = database.get(keyword);
		long endTime = System.currentTimeMillis();

		if (records != null) {
			return new AsyncResult<SearchResult>(new SearchResult(keyword, records, startTime, endTime));
		} else {
			throw new NotFound("the keyword '" + keyword + "'did not match any records");
		}

	}
}

class SearchResult {
	private final String keyword;
	private final List<String> records;
	private final long startTime;
	private final long endTime;

	SearchResult(String keyword, List<String> records, long startTime, long endTime) {
		this.keyword = keyword;
		this.records = records;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public String getKeyword() {
		return keyword;
	}

	public List<String> getRecords() {
		return records;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public long getTotalTime() {
		return endTime - startTime;
	}

}

class NotFound extends RuntimeException {

	NotFound(String msg) {
		super(msg);
	}

}
