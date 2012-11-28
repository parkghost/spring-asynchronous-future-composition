package me.brandon.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		logger.info("starting application");

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "application-context.xml" });

		SearchApplication application = (SearchApplication) context.getBean("searchApplication");

		application.runWithFutureCallback();
		application.runWithAsyncFunction();

		// waiting for all asynchronous execution to complete
		try {
			application.await();
			logger.info("leaving application");
			context.destroy();
		} catch (InterruptedException e) {
			// nothing special
		}

	}
}