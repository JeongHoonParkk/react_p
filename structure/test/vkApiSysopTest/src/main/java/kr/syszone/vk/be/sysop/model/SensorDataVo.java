package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorDataVo  {

	private String sid;
	private String stype;
	private Double temperature;
	private Double humidity;
	private Double pressure;
	private Double light;
	private Double shock;
	private Long doorState;
	private Double battery;
	private Double voltage;
	private Double rssi;
	private String context;
	private Timestamp storeTime;
	private Timestamp time;

	@JsonCreator
	public SensorDataVo() {
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getStype() {
		return stype;
	}

	public void setStype(String stype) {
		this.stype = stype;
	}

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public Double getHumidity() {
		return humidity;
	}

	public void setHumidity(Double humidity) {
		this.humidity = humidity;
	}

	public Double getPressure() {
		return pressure;
	}

	public void setPressure(Double pressure) {
		this.pressure = pressure;
	}

	public Double getLight() {
		return light;
	}

	public void setLight(Double light) {
		this.light = light;
	}

	public Double getShock() {
		return shock;
	}

	public void setShock(Double shock) {
		this.shock = shock;
	}

	public Long getDoorState() {
		return doorState;
	}

	public void setDoorState(Long doorState) {
		this.doorState = doorState;
	}

	public Double getBattery() {
		return battery;
	}

	public void setBattery(Double battery) {
		this.battery = battery;
	}

	public Double getVoltage() {
		return voltage;
	}

	public void setVoltage(Double voltage) {
		this.voltage = voltage;
	}

	public Double getRssi() {
		return rssi;
	}

	public void setRssi(Double rssi) {
		this.rssi = rssi;
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
