package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.SensorAssign;

public class SensorAssignVo {

	private Long id;
	private String name;
	private String sId;
	private String companyId;
	private String memo;

	private SensorVo sensor;

	@JsonCreator
	public SensorAssignVo() {
	}

	public SensorAssignVo(SensorAssign sa) {
		this.id = sa.getId();
		this.name = sa.getName();
		this.sId = sa.getSid();
		this.companyId = sa.getCompanyId();
		this.memo = sa.getMemo();
	}

	public SensorAssignVo(SensorAssign sa, Long siId, String sensorMemo, Long sensorInfoId, String sensorMaker,
			String sensorModel, Integer netType, Integer dataType) {
		this.id = sa.getId();
		this.name = sa.getName();
		this.sId = sa.getSid();
		this.companyId = sa.getCompanyId();
		this.memo = sa.getMemo();

		this.sensor = new SensorVo();
		sensor.setId(this.sId);
		sensor.setSiId(sensorInfoId);
		sensor.setMemo(sensorMemo);
		sensor.setMaker(sensorMaker);
		sensor.setModel(sensorModel);
		sensor.setNetType(netType);
		sensor.setDataType(dataType);
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

	public String getsId() {
		return sId;
	}

	public void setsId(String sId) {
		this.sId = sId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public SensorVo getSensor() {
		return sensor;
	}

	public void setSensor(SensorVo sensor) {
		this.sensor = sensor;
	}

}
