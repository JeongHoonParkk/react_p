package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorAddForCompanyVo {

	private String sensorId;
	private String sensorName;
	private Long sensorInfoId;
	private String work;

	@JsonCreator
	public SensorAddForCompanyVo() {
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public Long getSensorInfoId() {
		return sensorInfoId;
	}

	public void setSensorInfoId(Long sensorInfoId) {
		this.sensorInfoId = sensorInfoId;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

}
