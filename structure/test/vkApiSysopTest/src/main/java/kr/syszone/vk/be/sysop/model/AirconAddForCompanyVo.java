package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AirconAddForCompanyVo {

	private String airconId;
	private String airconName;
	private Long airconInfoId;
	private String work;

	@JsonCreator
	public AirconAddForCompanyVo() {
	}

	public String getAirconId() {
		return airconId;
	}

	public void setAirconId(String airconId) {
		this.airconId = airconId;
	}

	public String getAirconName() {
		return airconName;
	}

	public void setAirconName(String airconName) {
		this.airconName = airconName;
	}

	public Long getAirconInfoId() {
		return airconInfoId;
	}

	public void setAirconInfoId(Long airconInfoId) {
		this.airconInfoId = airconInfoId;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

}
