package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CompanyVehicleApis {
	private String version = "1.0.0";

	@JsonCreator
	public CompanyVehicleApis() {
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
