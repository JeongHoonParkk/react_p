package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.Order;
import kr.syszone.vk.be.db.entity.OrderDelivery;

public class OrderTBVo {

	private Long id;
	private String name;
	private Integer orderType;
	private String orderTypeName;
	private String orderCompanyId;
	private String orderCompanyName;
	private String executeCompanyId;
	private String executeCompanyName;
	private Integer state;
	private Timestamp tsRegister;
	private Timestamp tsStart;
	private Timestamp tsEnd;
	private String memo;

//	private List<String> sensorId;
//	private List<String> gnssId;

	private Long vehicleId;

//	private Long productId;
//	private String productName;
//	private Double productTL;
//	private Double productTH;

	private String fromName;
	private String fromPostcode;
	private String fromAddressPost;
	private String fromAddressDetail;
	private Double fromLatitude;
	private Double fromLongitude;
	private String toName;
	private String toPostcode;
	private String toAddressPost;
	private String toAddressDetail;
	private Double toLatitude;
	private Double toLongitude;

	@JsonCreator
	public OrderTBVo() {

	}

	public OrderTBVo(Order aOrder, String otName, String ocName, String ecName, OrderDelivery aOd) {
		this.id = aOrder.getId();
		this.name = aOrder.getName();
		this.orderType = aOrder.getOrderType();
		this.orderTypeName = otName;
		this.orderCompanyId = aOrder.getOrderCompanyId();
		this.orderCompanyName = ocName;
		this.executeCompanyId = aOrder.getExecuteCompanyId();
		this.executeCompanyName = ecName;
		this.state = aOrder.getState();
		this.tsRegister = aOrder.getTsRegister();
		this.tsStart = aOrder.getTsStart();
		this.tsEnd = aOrder.getTsEnd();
		this.memo = aOrder.getMemo();

		this.fromName = aOd.getFromName();
		this.fromAddressPost = aOd.getFromAddressPost();
		this.fromAddressDetail = aOd.getFromAddressDetail();
		this.fromPostcode = aOd.getFromPostcode();
		this.fromLatitude = aOd.getFromLatitude();
		this.fromLongitude = aOd.getFromLongitude();
		this.toName = aOd.getToName();
		this.toPostcode = aOd.getToPostcode();
		this.toAddressPost = aOd.getToAddressPost();
		this.toAddressDetail = aOd.getToAddressDetail();
		this.toLatitude = aOd.getToLatitude();
		this.toLongitude = aOd.getToLongitude();

//		this.productId = pId;
//		this.productName = pName;
//		this.productTL = productTL;
//		this.productTH = productTH;

	}

	public OrderTBVo(Order aOrder, String otName, String ocName, String ecName) {
		this.id = aOrder.getId();
		this.name = aOrder.getName();
		this.orderType = aOrder.getOrderType();
		this.orderTypeName = otName;
		this.orderCompanyId = aOrder.getOrderCompanyId();
		this.orderCompanyName = ocName;
		this.executeCompanyId = aOrder.getExecuteCompanyId();
		this.executeCompanyName = ecName;
		this.state = aOrder.getState();
		this.tsRegister = aOrder.getTsRegister();
		this.tsStart = aOrder.getTsStart();
		this.tsEnd = aOrder.getTsEnd();
		this.memo = aOrder.getMemo();
//
//		this.productId = pId;
//		this.productName = pName;

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getOrderType() {
		return orderType;
	}

	public void setOrderType(Integer orderType) {
		this.orderType = orderType;
	}

	public String getOrderTypeName() {
		return orderTypeName;
	}

	public void setOrderTypeName(String orderTypeName) {
		this.orderTypeName = orderTypeName;
	}

	public String getOrderCompanyId() {
		return orderCompanyId;
	}

	public void setOrderCompanyId(String orderCompanyId) {
		this.orderCompanyId = orderCompanyId;
	}

	public String getOrderCompanyName() {
		return orderCompanyName;
	}

	public void setOrderCompanyName(String orderCompanyName) {
		this.orderCompanyName = orderCompanyName;
	}

	public String getExecuteCompanyId() {
		return executeCompanyId;
	}

	public void setExecuteCompanyId(String executeCompanyId) {
		this.executeCompanyId = executeCompanyId;
	}

	public String getExecuteCompanyName() {
		return executeCompanyName;
	}

	public void setExecuteCompanyName(String executeCompanyName) {
		this.executeCompanyName = executeCompanyName;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Timestamp getTsRegister() {
		return tsRegister;
	}

	public void setTsRegister(Timestamp tsRegister) {
		this.tsRegister = tsRegister;
	}

	public Timestamp getTsStart() {
		return tsStart;
	}

	public void setTsStart(Timestamp tsStart) {
		this.tsStart = tsStart;
	}

	public Timestamp getTsEnd() {
		return tsEnd;
	}

	public void setTsEnd(Timestamp tsEnd) {
		this.tsEnd = tsEnd;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
	
	public Long getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Long vehicleId) {
		this.vehicleId = vehicleId;
	}
//	public List<String> getSensorId() {
//		return sensorId;
//	}
//
//	public void setSensorId(List<String> sensorId) {
//		this.sensorId = sensorId;
//	}
//
//	public List<String> getGnssId() {
//		return gnssId;
//	}
//
//	public void setGnssId(List<String> gnssId) {
//		this.gnssId = gnssId;
//	}

//	public Long getProductId() {
//		return productId;
//	}
//
//	public void setProductId(Long productId) {
//		this.productId = productId;
//	}
//
//	public String getProductName() {
//		return productName;
//	}
//
//	public void setProductName(String productName) {
//		this.productName = productName;
//	}
//
//	public Double getProductTL() {
//		return productTL;
//	}
//
//	public void setProductTL(Double productTL) {
//		this.productTL = productTL;
//	}
//
//	public Double getProductTH() {
//		return productTH;
//	}
//
//	public void setProductTH(Double productTH) {
//		this.productTH = productTH;
//	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getFromPostcode() {
		return fromPostcode;
	}

	public void setFromPostcode(String fromPostcode) {
		this.fromPostcode = fromPostcode;
	}

	public String getFromAddressPost() {
		return fromAddressPost;
	}

	public void setFromAddressPost(String fromAddressPost) {
		this.fromAddressPost = fromAddressPost;
	}

	public String getFromAddressDetail() {
		return fromAddressDetail;
	}

	public void setFromAddressDetail(String fromAddressDetail) {
		this.fromAddressDetail = fromAddressDetail;
	}

	public Double getFromLatitude() {
		return fromLatitude;
	}

	public void setFromLatitude(Double fromLatitude) {
		this.fromLatitude = fromLatitude;
	}

	public Double getFromLongitude() {
		return fromLongitude;
	}

	public void setFromLongitude(Double fromLongitude) {
		this.fromLongitude = fromLongitude;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public String getToPostcode() {
		return toPostcode;
	}

	public void setToPostcode(String toPostcode) {
		this.toPostcode = toPostcode;
	}

	public String getToAddressPost() {
		return toAddressPost;
	}

	public void setToAddressPost(String toAddressPost) {
		this.toAddressPost = toAddressPost;
	}

	public String getToAddressDetail() {
		return toAddressDetail;
	}

	public void setToAddressDetail(String toAddressDetail) {
		this.toAddressDetail = toAddressDetail;
	}

	public Double getToLatitude() {
		return toLatitude;
	}

	public void setToLatitude(Double toLatitude) {
		this.toLatitude = toLatitude;
	}

	public Double getToLongitude() {
		return toLongitude;
	}

	public void setToLongitude(Double toLongitude) {
		this.toLongitude = toLongitude;
	}





}
