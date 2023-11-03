package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.Vehicle;

public class SelectCompanyVehicleVo {

	private Long vehicleId;
	private String vehicleNo;
	private String vehicleName;
	
	@JsonCreator
	public SelectCompanyVehicleVo() {

	}

	public SelectCompanyVehicleVo(Vehicle aVehicle, String vName) {
		this.vehicleId = aVehicle.getId();
		this.vehicleNo = aVehicle.getCarNo();
		this.vehicleName = vName;
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

	public String getVehicleName() {
		return vehicleName;
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}

	
}
