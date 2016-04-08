package skyler.tao.druidquery.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class UrlGenerator {

	private static Logger logger = Logger.getLogger(UrlGenerator.class);
	private static String url_uve;
	private static String url_info;

	static {
		Properties properties = new Properties();

		try {
			InputStream query_properties = UrlGenerator.class
					.getResourceAsStream("/query.properties");
			properties.load(query_properties);
			url_uve = properties.getProperty("url_uve");
			url_info = properties.getProperty("url_info");
			logger.info("Get query.properties!");
		} catch (Exception e) {
			logger.info("Fetch query.properties failed!");
		}
	}
	
	public static String getUrl_UVE() {
		return url_uve;
	}
	public static String getUrl_info() {
		return url_info;
	}
}
