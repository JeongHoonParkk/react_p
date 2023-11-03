package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PouchApis {
	private String version = "1.0.0";

	@JsonCreator
	public PouchApis() {
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
