package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.CompanyVehicle;

public class CompanyVehicleVo {

	private Long id;
	private String companyId;
	private String companyName;
	private Long vehicleId;
	private String vehicleNo;
	private String userId;
	private String userName;
	private String name;
	private String memo;

	@JsonCreator
	public CompanyVehicleVo() {

	}

	public CompanyVehicleVo(CompanyVehicle aCompanyVehicle, String cName, String vCarNo, String uName) {
		this.id = aCompanyVehicle.getId();
		this.companyId = aCompanyVehicle.getCompanyId();
		this.companyName = cName;
		this.vehicleId = aCompanyVehicle.getVehicleId();
		this.vehicleNo = vCarNo;
		this.userId = aCompanyVehicle.getUserId();
		this.userName = uName;
		this.name = aCompanyVehicle.getName();
		this.memo = aCompanyVehicle.getMemo();
	}

	public CompanyVehicleVo(CompanyVehicle cv) {
		this.id = cv.getId();
		this.companyId = cv.getCompanyId();
		this.vehicleId = cv.getVehicleId();
		this.userId = cv.getUserId();
		this.name = cv.getName();
		this.memo = cv.getMemo();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Long getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Long vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getVehicleNo() {
		return vehicleNo;
	}

	public void setVehicleNo(String vehicleNo) {
		this.vehicleNo = vehicleNo;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	

}
