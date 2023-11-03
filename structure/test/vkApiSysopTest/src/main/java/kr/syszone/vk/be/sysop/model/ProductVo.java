package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.Product;
import kr.syszone.vk.be.db.entity.ProductHistory;

public class ProductVo {

	private Long id;
	private String ownerId;
	private String ownerName;
	private String managerId;
	private String managerName;
	private String name;
	private Integer amount;
	private Timestamp tsRegister;
	private Timestamp tsModify;
	private Double tempLow;
	private Double tempHigh;
	private Integer state;
	private Integer torSec;
	private Long refId;
	private String refName;
	private String memo;
	private List<ProductHistory> phList;

	@JsonCreator
	public ProductVo() {

	}

	public ProductVo(Product aProduct, String ocName, String mcName, String refName) {

		this.id = aProduct.getId();
		this.ownerId = aProduct.getOwnerId();
		this.ownerName = ocName;
		this.managerId = aProduct.getManagerId();
		this.managerName = mcName;
		this.name = aProduct.getName();
		this.amount = aProduct.getAmount();
		this.tempHigh = aProduct.getTempHigh();
		this.tempLow = aProduct.getTempLow();
		this.tsRegister = aProduct.getTsRegister();
		this.tsModify = aProduct.getTsModify();
		this.state = aProduct.getState();
		this.torSec = aProduct.getTorSec();
		this.refId = aProduct.getRefId();
		this.refName = refName;
		this.memo = aProduct.getMemo();

	}

	public ProductVo(Product aProduct) {
		this.id = aProduct.getId();
		this.ownerId = aProduct.getOwnerId();
		this.managerId = aProduct.getManagerId();
		this.name = aProduct.getName();
		this.amount = aProduct.getAmount();
		this.tempHigh = aProduct.getTempHigh();
		this.tempLow = aProduct.getTempLow();
		this.tsModify = aProduct.getTsModify();
		this.state = aProduct.getState();
		this.torSec = aProduct.getTorSec();
		this.refId = aProduct.getRefId();
		this.memo = aProduct.getMemo();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getManagerId() {
		return managerId;
	}

	public void setManagerId(String managerId) {
		this.managerId = managerId;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getTsRegister() {
		return tsRegister;
	}

	public void setTsRegister(Timestamp tsRegister) {
		this.tsRegister = tsRegister;
	}

	public Timestamp getTsModify() {
		return tsModify;
	}

	public void setTsModify(Timestamp tsModify) {
		this.tsModify = tsModify;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getTorSec() {
		return torSec;
	}

	public void setTorSec(Integer torSec) {
		this.torSec = torSec;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Double getTempLow() {
		return tempLow;
	}

	public void setTempLow(Double tempLow) {
		this.tempLow = tempLow;
	}

	public Double getTempHigh() {
		return tempHigh;
	}

	public void setTempHigh(Double tempHigh) {
		this.tempHigh = tempHigh;
	}

	public Long getRefId() {
		return refId;
	}

	public void setRefId(Long refId) {
		this.refId = refId;
	}

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	public List<ProductHistory> getPhList() {
		return phList;
	}

	public void setPhList(List<ProductHistory> phList) {
		this.phList = phList;
	}
	
	

}
