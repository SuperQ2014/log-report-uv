package skyler.tao.druidquery.main;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import skyler.tao.druidquery.process.AllPlatformProcess;
import skyler.tao.druidquery.process.AllPlatformProductProcess;
import skyler.tao.druidquery.process.AllProductProcess;
import skyler.tao.druidquery.process.GroupByProcess;

public class QueryMain {

	private static final int NTHREADS = 4;
	private static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
	
	public static void main(String[] args) {
		exec.execute(new AllPlatformProcess());
		exec.execute(new AllProductProcess());
		exec.execute(new GroupByProcess());
		exec.execute(new AllPlatformProductProcess());
		
		exec.shutdown();
	}
}
