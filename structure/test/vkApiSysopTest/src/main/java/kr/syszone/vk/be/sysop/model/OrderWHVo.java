package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.Order;

public class OrderWHVo {

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
	private Long productId;
	private String productName;
	

	@JsonCreator
	public OrderWHVo() {

	}

	


	public OrderWHVo(Order od) {
		this.id = od.getId();
		this.name = od.getName();
		this.orderType = od.getOrderType();
		this.orderCompanyId = od.getOrderCompanyId();
		this.executeCompanyId = od.getExecuteCompanyId();
		this.state = od.getState();
		this.tsRegister = od.getTsRegister();
		this.tsStart = od.getTsStart();
		this.tsEnd = od.getTsEnd();
		this.memo = od.getMemo();
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

	

}
