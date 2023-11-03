package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import kr.syszone.vk.be.db.entity.Product;

public class OrderTBDetailVo {

	@JsonCreator
	public OrderTBDetailVo() {

	}

	private String sensorId;
	private String gnssId;

	private Long productId;
	private String productName;
	private Double productTL;
	private Double productTH;

	public OrderTBDetailVo(Product p) {
		this.productId = p.getId();
		this.productName = p.getName();
		this.productTL = p.getTempLow();
		this.productTH = p.getTempHigh();
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getGnssId() {
		return gnssId;
	}

	public void setGnssId(String gnssId) {
		this.gnssId = gnssId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Double getProductTL() {
		return productTL;
	}

	public void setProductTL(Double productTL) {
		this.productTL = productTL;
	}

	public Double getProductTH() {
		return productTH;
	}

	public void setProductTH(Double productTH) {
		this.productTH = productTH;
	}

}
