package skyler.tao.druidquery.process;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import skyler.tao.druidquery.mybatis.MyBatisConnectionFactory;
import skyler.tao.druidquery.mybatis.ReportTarget;
import skyler.tao.druidquery.mybatis.ReportTargetDAO;

public class GroupByProcess extends ProcessAbstract implements Runnable {

	private Logger logger = Logger.getLogger(GroupByProcess.class);
	private ReportTargetDAO uvReportDAO = new ReportTargetDAO(MyBatisConnectionFactory.getSqlSessionFactory());
	private static final int NTHREADS = 4;
	private static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);

	@Override
	public void run() {

		String body = "{\"queryType\":\"groupBy\",\"dataSource\":\"bo_adid\",\"granularity\":{\"type\":\"period\",\"period\":\"P1D\",\"timeZone\":\"Asia/Shanghai\"},\"dimensions\":[\"service_name\",\"platform\",\"product\"],\"aggregations\":[{\"type\":\"hyperUnique\",\"name\":\"imp_uv\",\"fieldName\":\"uv\"}],\"intervals\":[\"" + dateGenerate.getStartDate() + "T16:00:00/" + dateGenerate.getEndDate() + "T16:00:00\"]}";
		JsonElement responseJsonAll = postRequest.http(url_uve, body);
		while (responseJsonAll == null) {
			logger.warn("Response empty: " + body);
			responseJsonAll = postRequest.http(url_uve, body);
		}

		if (responseJsonAll.isJsonArray()) {
			JsonArray responseJsonArray = responseJsonAll.getAsJsonArray();
			int length = responseJsonArray.size();
			logger.info("Response data length: " + length);
			Collection<Future<?>> futures = new LinkedList<Future<?>>();
			
			int increment = length / 4;
			for(int i = 0; i < 3; i++) {
				futures.add(exec.submit(new Worker(i * increment, (i + 1) * increment, responseJsonArray)));
			}
			//使用上述for循环，最后一个可能会少
			futures.add(exec.submit(new Worker(3 * increment, length, responseJsonArray)));
			for (Future<?> future:futures) {
			    try {
					future.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			exec.shutdown();
		}
	}
	private class Worker implements Runnable {
	    final private int minIndex; // first index, inclusive
	    final private int maxIndex; // last index, exclusive
	    private JsonArray data;
	    private String date = dateGenerate.getDate();
	    private String service_name = "_";
	    private String platform = "_";
	    private String product = "_";
	    private int uv = 0;
	    private int imp_uv = 0;

	    public Worker(int minIndex, int maxIndex, JsonArray data) {
	        this.minIndex = minIndex;
	        this.maxIndex = maxIndex;
	        this.data = data;
	    }

	    public void run() {
	    	for (int i = minIndex; i < maxIndex; i++) {
				JsonObject event = (JsonObject) data.get(i).getAsJsonObject().get("event");
				if (event != null) {
					try {
						JsonElement serviceNameJson = event.get("service_name");
						if (!serviceNameJson.isJsonNull()) {
							service_name = serviceNameJson.getAsString();
						}
					} catch (Exception e) {
						logger.warn("Service_name cannot be parsed!");
						continue;
					}
					try {
						JsonElement platformJson = event.get("platform");
						if (!platformJson.isJsonNull()) {
							platform = platformJson.getAsString();
						}
					} catch (Exception e) {
						logger.warn("Platform cannot be parsed!");
						continue;
					}
					try {
						JsonElement prJson = event.get("product"); 
						if (!prJson.isJsonNull()) {
							product = prJson.getAsString();
						}
					} catch (Exception e) {
						logger.warn("Pr cannot be parsed!");
						continue;
					}
					try {
						JsonElement imp_uvJson = event.get("imp_uv");
						if (!imp_uvJson.isJsonNull()) {
							imp_uv = imp_uvJson.getAsInt();
						}
					} catch (Exception e) {
						logger.error("Imp_uv cannot be parsed!");
						continue;
					}

					String uv_body = "{\"queryType\":\"timeseries\",\"dataSource\":\"uve_stat_report\",\"granularity\":{\"type\":\"period\",\"period\":\"P1D\",\"timeZone\":\"Asia/Shanghai\"},\"intervals\":[\""+dateGenerate.getStartDate()+"T16:00:00/"+dateGenerate.getEndDate()+"T16:00:00\"],\"aggregations\":[{\"type\":\"hyperUnique\",\"fieldName\":\"uv1\",\"name\":\"uv\"}],\"filter\":{\"type\":\"and\",\"fields\":[{\"type\":\"selector\",\"dimension\":\"service_name\",\"value\":\"" + service_name + "\"},{\"type\":\"selector\",\"dimension\":\"platform\",\"value\":\""+platform+"\"},{\"type\":\"regex\",\"dimension\":\"product_r\",\"pattern\":\""+product+"\"}]}}";
					JsonElement uv_responseJson = postRequest.http(url_info, uv_body);
					while (uv_responseJson == null) {
						logger.warn("uv response null: " + uv_body);
						uv_responseJson = postRequest.http(url_info, uv_body);
					}
					if (uv_responseJson.isJsonArray()) {
						JsonArray uv_responseJsonArray = uv_responseJson.getAsJsonArray();
						try {
							uv = uv_responseJsonArray.get(0).getAsJsonObject().get("result").getAsJsonObject().get("uv").getAsInt();
						} catch (Exception e) {
							logger.error("uv response error!");
							continue;
						}
					}
				}
				
				ReportTarget target = new ReportTarget();
				target.setDate(date);
				target.setService_name(service_name);
				target.setPlatform(platform);
				target.setProduct(product);
				target.setUv(uv);
				target.setImp_uv(imp_uv);
				
				uvReportDAO.replace(target);
			}
	    }
	}
}