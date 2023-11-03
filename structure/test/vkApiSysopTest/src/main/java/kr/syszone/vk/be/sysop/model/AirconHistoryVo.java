package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.AirconHistory;

public class AirconHistoryVo {

	private Long id;
	private String aid;
	private String companyId;
	private String work;
	private Timestamp tsWork;

	@JsonCreator
	public AirconHistoryVo() {
	}

	public AirconHistoryVo(AirconHistory ah) {
		this.id = ah.getId();
		this.aid = ah.getAid();
		this.companyId = ah.getCompanyId();
		this.work = ah.getWork();
		this.tsWork = ah.getTsWork();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public Timestamp getTsWork() {
		return tsWork;
	}

	public void setTsWork(Timestamp tsWork) {
		this.tsWork = tsWork;
	}

}
