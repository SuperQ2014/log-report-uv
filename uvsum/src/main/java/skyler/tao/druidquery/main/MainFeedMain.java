package skyler.tao.druidquery.main;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import skyler.tao.druidquery.process.AllPlatformProcess;
import skyler.tao.druidquery.process.AllPlatformProductProcess;
import skyler.tao.druidquery.process.AllProductProcess;
import skyler.tao.druidquery.process.GroupByProcess;

public class MainFeedMain {

	private static final int NTHREADS = 4;
	private static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
	private static final Logger logger = Logger.getLogger(MainFeedMain.class);
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		long startTime = System.currentTimeMillis();
		
		Collection<Future<?>> futures = new LinkedList<Future<?>>();
		futures.add(exec.submit(new AllPlatformProcess()));
		futures.add(exec.submit(new AllProductProcess()));
		futures.add(exec.submit(new GroupByProcess()));
		futures.add(exec.submit(new AllPlatformProductProcess()));

		for (Future<?> future:futures) {
		    future.get();
		}
		exec.shutdown();
		
		long endTime = System.currentTimeMillis();
		
		logger.info("startTime: " + startTime);
		logger.info("endTime: " + endTime);
		logger.info("Time takes: " + (endTime - startTime)/1000 + " s");
		
	}
}
