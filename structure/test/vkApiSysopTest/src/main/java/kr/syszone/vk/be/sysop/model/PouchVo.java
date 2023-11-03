package kr.syszone.vk.be.sysop.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import kr.syszone.vk.be.db.entity.Pouch;

public class PouchVo {

	private Long id;
	private String companyName;
	private String name;
	private Timestamp tsRegister;
	private String memo;

	private String companyId;

	@JsonCreator
	public PouchVo() {

	}

	public PouchVo(Pouch pouch, String companyName, String companyId) {
		this.id = pouch.getId();
		this.companyId = companyId;
		this.companyName = companyName;
		this.name = pouch.getName();
		this.tsRegister = pouch.getTsRegister();
		this.memo = pouch.getMemo();
	}

	//Pouch Add 회사목록 VO
	public PouchVo(String companyName, String companyId) {
		this.companyName = companyName;
		this.companyId = companyId;
	}
	
	public PouchVo(Pouch pouch) {
		this.id = pouch.getId();
		this.companyId = pouch.getCompanyId();
		this.name = pouch.getName();
		this.tsRegister = pouch.getTsRegister();
		this.memo = pouch.getMemo();
	}

	public Pouch getPouch() {

		Pouch pouch = new Pouch();

		pouch.setId(id);
		pouch.setCompanyId(companyId);
		pouch.setName(name);
		pouch.setTsRegister(tsRegister);
		pouch.setMemo(memo);

		return pouch;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getTsRegister() {
		return tsRegister;
	}

	public void setTsRegister(Timestamp tsRegister) {
		this.tsRegister = tsRegister;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

}
