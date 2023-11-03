package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.SensorHistory;

public class SensorHistoryVo {

	private Long id;
	private String sid;
	private String companyId;
	private String work;
	private Timestamp tsWork;

	@JsonCreator
	public SensorHistoryVo() {
	}

	public SensorHistoryVo(SensorHistory sh) {
		this.id = sh.getId();
		this.sid = sh.getSid();
		this.companyId = sh.getCompanyId();
		this.work = sh.getWork();
		this.tsWork = sh.getTsWork();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
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
