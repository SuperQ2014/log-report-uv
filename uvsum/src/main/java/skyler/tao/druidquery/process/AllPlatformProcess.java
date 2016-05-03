package skyler.tao.druidquery.process;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import skyler.tao.druidquery.mybatis.MyBatisConnectionFactory;
import skyler.tao.druidquery.mybatis.ReportTarget;
import skyler.tao.druidquery.mybatis.ReportTargetDAO;

public class AllPlatformProcess extends ProcessAbstract implements Runnable {

	private Logger logger = Logger.getLogger(AllPlatformProcess.class);
	private ReportTargetDAO uvReportDAO = new ReportTargetDAO(MyBatisConnectionFactory.getSqlSessionFactory());
	@Override
	public void run() {
		String body = "{\"queryType\":\"groupBy\",\"dataSource\":\"bo_adid\",\"granularity\":{\"type\":\"period\",\"period\":\"P1D\",\"timeZone\":\"Asia/Shanghai\"},\"dimensions\":[\"service_name\"],\"aggregations\":[{\"type\":\"hyperUnique\",\"name\":\"imp_uv\",\"fieldName\":\"uv\"}],\"intervals\":[\"" + dateGenerate.getStartDate() + "T16:00:00/" + dateGenerate.getEndDate() + "T16:00:00\"]}";
		JsonElement responseJsonAll = postRequest.http(url_uve, body);
		while (responseJsonAll == null) {
			logger.warn("Response empty: " + body);
			responseJsonAll = postRequest.http(url_uve, body);
		}

		if (responseJsonAll.isJsonArray()) {
			JsonArray responseJsonArray = responseJsonAll.getAsJsonArray();
			int length = responseJsonArray.size();
			logger.info("Response data length: " + length);

			String date = dateGenerate.getDate();
			String service_name = "_";
			String platform = "all";
			String product = "all";
			int uv = 0;
			int imp_uv = 0;

			for (int i = 0; i < length; i++) {
				JsonObject event = (JsonObject) responseJsonArray.get(i).getAsJsonObject().get("event");
				if (event != null) {
					
					try {
						JsonElement serviceNameJson = event.get("service_name");
						if (!serviceNameJson.isJsonNull()) {
							service_name = serviceNameJson.getAsString();
						}
					} catch (Exception e) {
						logger.error("Service_name cannot be parsed, return!");
						continue;
					}
					try {
						JsonElement imp_uvJson = event.get("imp_uv");
						if (!imp_uvJson.isJsonNull()) {
							imp_uv = imp_uvJson.getAsInt();
						}
					} catch (Exception e) {
						logger.error("Imp_uv cannot be parsed, return!");
						continue;
					}

					String uv_body = "{\"queryType\":\"timeseries\",\"dataSource\":\"uve_stat_report\",\"granularity\":{\"type\":\"period\",\"period\":\"P1D\",\"timeZone\":\"Asia/Shanghai\"},\"intervals\":[\""+dateGenerate.getStartDate()+"T16:00:00/"+dateGenerate.getEndDate()+"T16:00:00\"],\"aggregations\":[{\"type\":\"hyperUnique\",\"fieldName\":\"uv1\",\"name\":\"uv\"}],\"filter\":{\"type\":\"selector\",\"dimension\":\"service_name\",\"value\":\"" + service_name + "\"}}";
					JsonElement uv_responseJson = postRequest.http(url_info, uv_body);
					while (uv_responseJson == null) {
						logger.warn("Response empty: " + body);
						uv_responseJson = postRequest.http(url_info, uv_body);
					}
					if (uv_responseJson.isJsonArray()) {
						JsonArray uv_responseJsonArray = uv_responseJson.getAsJsonArray();
						try {
							uv = uv_responseJsonArray.get(0).getAsJsonObject().get("result").getAsJsonObject().get("uv").getAsInt();
						} catch (Exception e) {
							logger.error("Uv cannot be parsed, return!");
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
