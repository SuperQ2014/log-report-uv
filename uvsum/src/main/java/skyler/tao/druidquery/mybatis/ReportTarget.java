package skyler.tao.druidquery.mybatis;

public class ReportTarget {
	
	private String date;
	private String service_name;
	private String platform;
	private String product;
	
	private int uv;
	private int imp_uv;
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
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public int getUv() {
		return uv;
	}
	public void setUv(int uv) {
		this.uv = uv;
	}
	public int getImp_uv() {
		return imp_uv;
	}
	public void setImp_uv(int imp_uv) {
		this.imp_uv = imp_uv;
	}
	public String getService_name() {
		return service_name;
	}
	public void setService_name(String service_name) {
		this.service_name = service_name;
	}
}
