package skyler.tao.druidquery.process;

import skyler.tao.druidquery.util.DateGenerator;
import skyler.tao.druidquery.util.PostRequest;
import skyler.tao.druidquery.util.UrlGenerator;

public abstract class ProcessAbstract {

	protected static PostRequest postRequest = Enum.valueOf(PostRequest.class,
			"POSTREQUEST");
	protected static DateGenerator dateGenerate = Enum.valueOf(
			DateGenerator.class, "REQUIREDDATE");
	protected static String url_uve = UrlGenerator.getUrl_UVE();
	protected static String url_info = UrlGenerator.getUrl_info();
}
