package skyler.tao.druidquery.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import skyler.tao.druidquery.mybatis.MyBatisConnectionFactory;
import skyler.tao.druidquery.mybatis.ReportTarget;
import skyler.tao.druidquery.mybatis.ReportTargetDAO;
import skyler.tao.druidquery.process.AllPlatformProcess;
import skyler.tao.druidquery.process.AllPlatformServiceNameProcess;
import skyler.tao.druidquery.process.AllServiceNameProcess;
import skyler.tao.druidquery.process.GroupByProcess;
import skyler.tao.druidquery.util.DateGenerator;

public class QueryMain {

	private static final int NTHREADS = 4;
	private static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
	private static Logger logger = Logger.getLogger(QueryMain.class);
	protected static DateGenerator dateGenerate = Enum.valueOf(
			DateGenerator.class, "REQUIREDDATE");
	private static ReportTargetDAO uvReportDAO = new ReportTargetDAO(MyBatisConnectionFactory.getSqlSessionFactory());
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, SQLException {
		long startTime = System.currentTimeMillis();
		Collection<Future<?>> futures = new LinkedList<Future<?>>();
		
		futures.add(exec.submit(new AllPlatformProcess()));
		futures.add(exec.submit(new AllServiceNameProcess()));
		futures.add(exec.submit(new GroupByProcess()));
		futures.add(exec.submit(new AllPlatformServiceNameProcess()));
		
		for (Future<?> future:futures) {
		    future.get();
		}
		exec.shutdown();
		
		String date = dateGenerate.getDate();
//		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://10.77.96.56:3306/udatastats";
		String usr = "udatastats";
		String pwd = "2014udatastats";
		
		Connection con = DriverManager.getConnection(url, usr, pwd);
		Statement sql = con.createStatement();
		ResultSet rs1 = sql.executeQuery("select * from main_feed_uv_reports where date='" + date + "'");
		while (rs1.next()) {
			String platform = rs1.getString(2);
			String product = rs1.getString(3);
			int uv = rs1.getInt(4);
			int imp_uv = rs1.getInt(5);
			
			ReportTarget target = new ReportTarget();
			target.setDate(date);
			target.setService_name("main_feed");
			target.setPlatform(platform);
			target.setProduct(product);
			target.setUv(uv);
			target.setImp_uv(imp_uv);
			uvReportDAO.replace(target);
		}
		
		long endTime = System.currentTimeMillis();
		logger.info("startTime: " + startTime);
		logger.info("endTime: " + endTime);
		logger.info("Time takes: " + (endTime - startTime)/1000 + " s");
	}
}
