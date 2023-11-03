package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AirconDataVo {

	private String aid;
	private Double cfgTemp;
	private Integer runMode;
	private String context;
	private Timestamp storeTime;
	private Timestamp time;

	@JsonCreator
	public AirconDataVo() {
	}

	public String getAid() {
		return aid;
	}

	public void setAid(String aid) {
		this.aid = aid;
	}

	public Double getCfgTemp() {
		return cfgTemp;
	}

	public void setCfgTemp(Double cfgTemp) {
		this.cfgTemp = cfgTemp;
	}

	public Integer getRunMode() {
		return runMode;
	}

	public void setRunMode(Integer runMode) {
		this.runMode = runMode;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Timestamp getStoreTime() {
		return storeTime;
	}

	public void setStoreTime(Timestamp storeTime) {
		this.storeTime = storeTime;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

}
