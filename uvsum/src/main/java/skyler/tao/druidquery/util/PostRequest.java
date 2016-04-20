package skyler.tao.druidquery.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public enum PostRequest {

	POSTREQUEST;
	private final Logger logger = Logger.getLogger(PostRequest.class);

	public JsonElement http(String url, String body) {

		if (url == null || body == null) {
			logger.info("url or body is empty!");
			return null;
		}
		logger.info("post url: " + url);
		logger.info("post body: " + body);
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(body);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			RequestConfig paramsOther = RequestConfig.custom()
					.setConnectTimeout(990000).setSocketTimeout(990000).build();
			request.setConfig(paramsOther);
			HttpResponse response = httpClient.execute(request);
			String responseString = EntityUtils.toString(response.getEntity(),
					"UTF-8");
			JsonParser parser = new JsonParser();
			JsonElement jsonElement = parser.parse(responseString);
			JsonElement result = null;
			if (jsonElement.isJsonObject()) {
				result = jsonElement.getAsJsonObject();
			} else if (jsonElement.isJsonArray()) {
				result = jsonElement.getAsJsonArray();
			}

			if (result.isJsonNull()) {
				logger.info("Response: null");
				logger.info("HTTP POST BODY: " + body);
			}

			return result;

		} catch (IOException e) {
			logger.debug("HTTP post error!");
		}
		return null;
	}
}
