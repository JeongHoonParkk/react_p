package kr.syszone.vk.be.sysop.test.vsk.model;

import java.sql.Timestamp;

public class GnssData {

	private String mac;
	private Double gpsAccuracy;
	private Double gpsBearing;
	private Double gpsLat;
	private Double gpsLng;
	private Double gpsSpeed;
	private Long gpsTimestamp;
	private String power_state;
	private String ts_create;
	private String ts_unix;

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public Double getGpsAccuracy() {
		return gpsAccuracy;
	}

	public void setGpsAccuracy(Double gpsAccuracy) {
		this.gpsAccuracy = gpsAccuracy;
	}

	public Double getGpsBearing() {
		return gpsBearing;
	}

	public void setGpsBearing(Double gpsBearing) {
		this.gpsBearing = gpsBearing;
	}

	public Double getGpsLat() {
		return gpsLat;
	}

	public void setGpsLat(Double gpsLat) {
		this.gpsLat = gpsLat;
	}

	public Double getGpsLng() {
		return gpsLng;
	}

	public void setGpsLng(Double gpsLng) {
		this.gpsLng = gpsLng;
	}

	public Double getGpsSpeed() {
		return gpsSpeed;
	}

	public void setGpsSpeed(Double gpsSpeed) {
		this.gpsSpeed = gpsSpeed;
	}

	public Long getGpsTimestamp() {
		return gpsTimestamp;
	}

	public void setGpsTimestamp(Long gpsTimestamp) {
		this.gpsTimestamp = gpsTimestamp;
	}

	public String getPower_state() {
		return power_state;
	}

	public void setPower_state(String power_state) {
		this.power_state = power_state;
	}

	public String getTs_create() {
		return ts_create;
	}

	public void setTs_create(String ts_create) {
		this.ts_create = ts_create;
	}

	public String getTs_unix() {
		return ts_unix;
	}

	public void setTs_unix(String ts_unix) {
		this.ts_unix = ts_unix;
	}

	@Override
	public String toString() {
		return "GnssData [mac=" + mac + ", gpsAccuracy=" + gpsAccuracy + ", gpsBearing=" + gpsBearing + ", gpsLat="
				+ gpsLat + ", gpsLng=" + gpsLng + ", gpsSpeed=" + gpsSpeed + ", gpsTimestamp=" + gpsTimestamp
				+ ", power_state=" + power_state + ", ts_create=" + ts_create + ", ts_unix=" + ts_unix + "]";
	}

}
