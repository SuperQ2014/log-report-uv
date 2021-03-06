package skyler.tao.druidquery.process;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import skyler.tao.druidquery.mybatis.MyBatisConnectionFactory;
import skyler.tao.druidquery.mybatis.ReportTarget;
import skyler.tao.druidquery.mybatis.ReportTargetDAO;

public class AllPlatformProductProcess extends ProcessAbstract implements Runnable {

	private ReportTargetDAO uvReportDAO = new ReportTargetDAO(MyBatisConnectionFactory.getSqlSessionFactory());
	private Logger logger = Logger.getLogger(AllPlatformProductProcess.class);
	@Override
	public void run() {

		String date = dateGenerate.getDate();
		String platform = "all";
		String product = "all";
		int uv = 0;
		int imp_uv = 0;
		
		String impuv_body = "{\"queryType\":\"timeseries\",\"dataSource\":\"bo_adid\",\"granularity\":{\"type\":\"period\",\"period\":\"P1D\",\"timeZone\":\"Asia/Shanghai\"},\"intervals\":[\""+dateGenerate.getStartDate()+"T16:00:00/"+dateGenerate.getEndDate()+"T16:00:00\"],\"filter\":{\"type\":\"selector\",\"dimension\":\"service_name\",\"value\":\"main_feed\"},\"aggregations\":[{\"type\":\"hyperUnique\",\"fieldName\":\"uv\",\"name\":\"imp_uv\"}]}";
		JsonElement impuv_response = postRequest.http(url_uve, impuv_body);
		while (impuv_response == null) {
			logger.warn("imp_uv reponse empty: " + impuv_body);
			impuv_response = postRequest.http(url_uve, impuv_body);
		}

		if (impuv_response.isJsonArray()) {
			JsonArray impuv_response_array = impuv_response.getAsJsonArray();
			try {
				imp_uv = impuv_response_array.get(0).getAsJsonObject().get("result").getAsJsonObject().get("imp_uv").getAsInt();
				logger.info("imp_uv is: " + imp_uv);
			} catch (Exception e) {
				logger.error("imp_uv cannot be parsed, return!");
				return;
			}
		}
		
		String uv_body = "{\"queryType\":\"timeseries\",\"dataSource\":\"uve_stat_report\",\"granularity\":{\"type\":\"period\",\"period\":\"P1D\",\"timeZone\":\"Asia/Shanghai\"},\"intervals\":[\""+dateGenerate.getStartDate()+"T16:00:00/"+dateGenerate.getEndDate()+"T16:00:00\"],\"filter\":{\"type\":\"selector\",\"dimension\":\"service_name\",\"value\":\"main_feed\"},\"aggregations\":[{\"type\":\"hyperUnique\",\"fieldName\":\"uv1\",\"name\":\"uv\"}]}";
		JsonElement uv_response = postRequest.http(url_info, uv_body);
		while (uv_response == null) {
			logger.warn("uv reponse empty: " + uv_body);
			uv_response = postRequest.http(url_info, uv_body);
		}

		if (uv_response.isJsonArray()) {
			JsonArray uv_response_array = uv_response.getAsJsonArray();
			try {
				uv = uv_response_array.get(0).getAsJsonObject().get("result").getAsJsonObject().get("uv").getAsInt();
				logger.info("uv is: " + uv);
			} catch (Exception e) {
				logger.error("uv cannot be parsed, return!");
				return;
			}
		}
		
		ReportTarget target = new ReportTarget();
		target.setDate(date);
		target.setPlatform(platform);
		target.setProduct(product);
		target.setUv(uv);
		target.setImp_uv(imp_uv);
		
		uvReportDAO.replace(target);
	}

}
