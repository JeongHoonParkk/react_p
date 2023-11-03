package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CompanyUserApis {
	private String version = "1.0.0";

	@JsonCreator
	public CompanyUserApis() {
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
