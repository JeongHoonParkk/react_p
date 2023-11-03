package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.AirconAssign;

public class AirconAssignVo {

	private Long id;
	private String name;
	private String aid;
	private String companyId;
	private String memo;

	private AirconVo aircon;

	@JsonCreator
	public AirconAssignVo() {
	}

	public AirconAssignVo(AirconAssign aa) {
		this.id = aa.getId();
		this.name = aa.getName();
		this.aid = aa.getAid();
		this.companyId = aa.getCompanyId();
		this.memo = aa.getMemo();
	}

	public AirconAssignVo(AirconAssign aa, Long aiId, String airconMemo, Long airconInfoId, String airconMaker,
			String airconModel) {
		this.id = aa.getId();
		this.name = aa.getName();
		this.aid = aa.getAid();
		this.companyId = aa.getCompanyId();
		this.memo = aa.getMemo();

		this.aircon = new AirconVo();
		aircon.setId(this.aid);
		aircon.setAiId(airconInfoId);
		aircon.setMemo(airconMemo);
		aircon.setMaker(airconMaker);
		aircon.setModel(airconModel);
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

	public String getAid() {
		return aid;
	}

	public void setAid(String aid) {
		this.aid = aid;
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

	public AirconVo getAircon() {
		return aircon;
	}

	public void setAircon(AirconVo aircon) {
		this.aircon = aircon;
	}

}
