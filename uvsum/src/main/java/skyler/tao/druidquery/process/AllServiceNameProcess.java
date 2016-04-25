package skyler.tao.druidquery.process;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import skyler.tao.druidquery.mybatis.MyBatisConnectionFactory;
import skyler.tao.druidquery.mybatis.ReportTarget;
import skyler.tao.druidquery.mybatis.ReportTargetDAO;

public class AllServiceNameProcess extends ProcessAbstract implements Runnable {
	private Logger logger = Logger.getLogger(AllServiceNameProcess.class);
	private ReportTargetDAO uvReportDAO = new ReportTargetDAO(MyBatisConnectionFactory.getSqlSessionFactory());

	@Override
	public void run() {

		String body = "{\"queryType\":\"groupBy\",\"dataSource\":\"bo_adid\",\"granularity\":{\"type\":\"period\",\"period\":\"P1D\",\"timeZone\":\"Asia/Shanghai\"},\"dimensions\":[\"platform\"],\"aggregations\":[{\"type\":\"hyperUnique\",\"name\":\"imp_uv\",\"fieldName\":\"uv\"}],\"intervals\":[\"" + dateGenerate.getStartDate() + "T16:00:00/" + dateGenerate.getEndDate() + "T16:00:00\"]}";
		JsonElement responseJsonAll = postRequest.http(url_uve, body);
		while (responseJsonAll == null) {
			responseJsonAll = postRequest.http(url_uve, body);
		}

		if (responseJsonAll.isJsonArray()) {
			JsonArray responseJsonArray = responseJsonAll.getAsJsonArray();
			int length = responseJsonArray.size();
			logger.info("Response data length: " + length);

			String date = dateGenerate.getDate();
			String service_name = "all";
			String platform = "_";
			String product = "all";
			int uv = 0;
			int imp_uv = 0;

			for (int i = 0; i < length; i++) {
				JsonObject event = (JsonObject) responseJsonArray.get(i).getAsJsonObject().get("event");
				if (event != null) {
					try {
						JsonElement platformJson = event.get("platform");
						if (!platformJson.isJsonNull()) {
							platform = platformJson.getAsString();
						}
					} catch (Exception e) {
						logger.info("Platform cannot be parsed, use default!");
						platform = "_";
					}
					try {
						JsonElement imp_uvJson = event.get("imp_uv");
						if (!imp_uvJson.isJsonNull()) {
							imp_uv = imp_uvJson.getAsInt();
						}
					} catch (Exception e) {
						logger.info("Imp_uv cannot be parsed, use default!");
						imp_uv = 0;
					}

					String uv_body = "{\"queryType\":\"timeseries\",\"dataSource\":\"uve_stat_report\",\"granularity\":{\"type\":\"period\",\"period\":\"P1D\",\"timeZone\":\"Asia/Shanghai\"},\"intervals\":[\""+dateGenerate.getStartDate()+"T16:00:00/"+dateGenerate.getEndDate()+"T16:00:00\"],\"aggregations\":[{\"type\":\"hyperUnique\",\"fieldName\":\"uv1\",\"name\":\"uv\"}],\"filter\":{\"type\":\"selector\",\"dimension\":\"platform\",\"value\":\""+platform+"\"}}";
					JsonElement uv_responseJson = postRequest.http(url_info, uv_body);
					if (uv_responseJson == null) {
						logger.info("CONTINUE: Query uv failed! The " + i + " times.");
						continue;
					}
					if (uv_responseJson.isJsonArray()) {
						JsonArray uv_responseJsonArray = uv_responseJson.getAsJsonArray();
						try {
							uv = uv_responseJsonArray.get(0).getAsJsonObject().get("result").getAsJsonObject().get("uv").getAsInt();
						} catch (Exception e) {
							uv = 0;
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
