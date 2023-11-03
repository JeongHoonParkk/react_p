package kr.syszone.vk.be.sysop.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import kr.syszone.vk.be.db.entity.CompanyGroup;

public class CompanyGroupVo {

	private Long id;
	private String companyName;
	private int role;
	private String name;
	private String memo;
	private String companyId;

	@JsonCreator
	public CompanyGroupVo() {

	}

	public CompanyGroupVo(CompanyGroup companyGroup, String companyName, String companyId) {
		this.id = companyGroup.getId();
		this.companyId = companyId;
		this.companyName = companyName;
		this.role = companyGroup.getRole();
		this.name = companyGroup.getName();
		this.memo = companyGroup.getMemo();
	}

	// CompanyGroup Add 회사목록 VO
	public CompanyGroupVo(String companyName, String companyId) {
		this.companyName = companyName;
		this.companyId = companyId;
	}

	public CompanyGroupVo(CompanyGroup cg) {
		this.id = cg.getId();
		this.companyId = cg.getCompanyId();
		this.role = cg.getRole();
		this.name = cg.getName();
		this.memo = cg.getMemo();
	}

	public CompanyGroup getCompanyGroup() {

		CompanyGroup cg = new CompanyGroup();

		cg.setId(id);
		cg.setCompanyId(companyId);
		cg.setRole(role);
		cg.setName(name);
		cg.setMemo(memo);

		return cg;
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

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
