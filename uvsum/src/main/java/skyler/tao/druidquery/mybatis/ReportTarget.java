package skyler.tao.druidquery.mybatis;

public class ReportTarget {
	
	private String date;
	private String platform;
	private String pr;
	private String is_unread_pool;
	private String version;
	private String loadmore;
	private String service_name;
	
	public String getService_name() {
		return service_name;
	}

	public void setService_name(String service_name) {
		this.service_name = service_name;
	}

	private int uv;
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPr() {
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
	}

	public String getIs_unreadpool() {
		return is_unread_pool;
	}

	public void setIs_unreadpool(String is_unreadpool) {
		this.is_unread_pool = is_unreadpool;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLoadmore() {
		return loadmore;
	}

	public void setLoadmore(String loadmore) {
		this.loadmore = loadmore;
	}

	public int getUv() {
		return uv;
	}

	public void setUv(int uv) {
		this.uv = uv;
	}

	public String toString() {
		
		return  "[Date: " + date + 
				",service_name: " + service_name +
				",Platform: " + platform + 
				",pr: " + pr + 
				",is_unreadpool: " + is_unread_pool + 
				",version: " + version + 
				",loadmore: " + loadmore + 
				",uv: " + uv ;
	}
}
