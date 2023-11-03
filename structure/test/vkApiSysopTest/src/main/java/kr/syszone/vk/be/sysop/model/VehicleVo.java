package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.Vehicle;

public class VehicleVo {

	private Long id;
	private String carNo;
	private String carType;
	private Integer capacity;

	@JsonCreator
	public VehicleVo() {

	}

	public VehicleVo(Vehicle vehicle) {
		this.id = vehicle.getId();
		this.carNo = vehicle.getCarNo();
		this.carType = vehicle.getCarType();
		this.capacity = vehicle.getCapacity();
	}

	public Vehicle getVehicle() {

		Vehicle vehicle = new Vehicle();

		vehicle.setId(id);
		vehicle.setCarNo(carNo);
		vehicle.setCarType(carType);
		vehicle.setCapacity(capacity);

		return vehicle;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCarNo() {
		return carNo;
	}

	public void setCarNo(String carNo) {
		this.carNo = carNo;
	}

	public String getCarType() {
		return carType;
	}

	public void setCarType(String carType) {
		this.carType = carType;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	
}
